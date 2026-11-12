import json
import sys
from pathlib import Path

import pytest
import requests

import bootstrap_schema as bs
from conftest import FakeResponse, FakeSession

ENV = {
    "DATA_PARTITION": "osdu",
    "SCHEMA_URL": "http://schema",
    "ENTITLEMENTS_HOST": "http://entitlements",
    "OPENID_PROVIDER_URL": "http://keycloak/realms/osdu",
    "OPENID_PROVIDER_CLIENT_ID": "client",
    "OPENID_PROVIDER_CLIENT_SECRET": "very-secret",
}


@pytest.fixture
def no_sleep():
    delays = []
    return delays.append, delays


def happy_session():
    session = FakeSession()
    session.add("POST", "/protocol/openid-connect/token", FakeResponse(200, {"access_token": "tok"}))
    session.add("GET", "/api/entitlements/v2/groups", FakeResponse(200, "{}"))
    session.add("GET", "/api/schema-service/v1/info", FakeResponse(200, "{}"))
    session.add("PUT", "/api/schema-service/v1/schemas/system", FakeResponse(201, "{}"))
    return session


# ---------------------------------------------------------------- env


@pytest.mark.parametrize("missing", bs.REQUIRED_ENV)
def test_every_required_variable_is_validated(missing):
    env = {key: value for key, value in ENV.items() if key != missing}
    with pytest.raises(bs.BootstrapError, match=missing):
        bs.validate_env(env)


# ---------------------------------------------------------------- token


def test_get_access_token_posts_client_credentials():
    session = FakeSession()
    session.add("POST", "/protocol/openid-connect/token", FakeResponse(200, {"access_token": "tok"}))
    assert bs.get_access_token(ENV, session) == "tok"
    _, url, kwargs = session.calls[0]
    assert url == "http://keycloak/realms/osdu/protocol/openid-connect/token"
    assert kwargs["data"] == {
        "grant_type": "client_credentials",
        "scope": "openid",
        "client_id": "client",
        "client_secret": "very-secret",
    }


@pytest.mark.parametrize(
    "response",
    [
        FakeResponse(200, {"error": "invalid_client"}),
        FakeResponse(200, {"access_token": None}),
        FakeResponse(502, "<html>bad gateway</html>"),
        requests.ConnectionError("down"),
    ],
)
def test_get_access_token_failures(response):
    session = FakeSession()
    session.add("POST", "/protocol/openid-connect/token", response)
    with pytest.raises(bs.BootstrapError, match="access token"):
        bs.get_access_token(ENV, session)


# ---------------------------------------------------------------- wait loops


def test_wait_for_retries_until_reachable(no_sleep):
    sleep, delays = no_sleep
    session = FakeSession()
    session.add("GET", "/info", FakeResponse(503), requests.ConnectionError("x"), FakeResponse(200))
    bs.wait_for("Schema service", "http://schema/info", session, None, sleep)
    assert delays == [bs.RETRY_DELAY, bs.RETRY_DELAY]
    assert len(session.calls) == 3


def test_wait_for_gives_up_after_max_retries(no_sleep):
    sleep, delays = no_sleep
    session = FakeSession()
    session.add("GET", "/info", FakeResponse(503))
    with pytest.raises(bs.BootstrapError, match="did not become reachable"):
        bs.wait_for("Schema service", "http://schema/info", session, None, sleep)
    assert len(session.calls) == bs.MAX_RETRIES
    assert len(delays) == bs.MAX_RETRIES


# ---------------------------------------------------------------- full run


def test_run_bootstraps_and_writes_the_ready_file(schemas_tree, tmp_path, no_sleep, capsys):
    sleep, delays = no_sleep
    session = happy_session()
    ready = tmp_path / "ready"

    bs.run(ENV, session, sleep, dest=schemas_tree, ready_file=str(ready))

    assert ready.exists()
    assert bs.ENTITLEMENTS_SETTLE_SECONDS in delays
    entitlements = next(call for call in session.calls if "entitlements" in call[1])
    assert entitlements[2]["headers"]["data-partition-id"] == "osdu"
    assert entitlements[2]["headers"]["Authorization"] == "Bearer tok"
    put = next(call for call in session.calls if call[0] == "PUT")
    assert put[1] == "http://schema/api/schema-service/v1/schemas/system"
    assert put[2]["headers"]["Authorization"] == "Bearer tok"
    out = capsys.readouterr().out
    assert "very-secret" not in out and "tok\n" not in out
    assert "Bootstrap finished successfully" in out


def test_run_fails_when_registration_reports_failures(schemas_tree, tmp_path, no_sleep):
    sleep, _ = no_sleep
    session = FakeSession()
    session.add("POST", "/protocol/openid-connect/token", FakeResponse(200, {"access_token": "tok"}))
    session.add("GET", "/api/entitlements/v2/groups", FakeResponse(200))
    session.add("GET", "/api/schema-service/v1/info", FakeResponse(200))
    session.add("PUT", "/schemas/system", FakeResponse(400, json.dumps({"error": {"message": "Invalid schema"}})))
    ready = tmp_path / "ready"
    with pytest.raises(bs.BootstrapError, match="failed to register"):
        bs.run(ENV, session, sleep, dest=schemas_tree, ready_file=str(ready))
    assert not ready.exists()


def test_run_fails_on_early_exit_errors(schemas_tree, tmp_path, no_sleep):
    sleep, _ = no_sleep
    session = FakeSession()
    session.add("POST", "/protocol/openid-connect/token", FakeResponse(200, {"access_token": "tok"}))
    session.add("GET", "/api/entitlements/v2/groups", FakeResponse(200))
    session.add("GET", "/api/schema-service/v1/info", FakeResponse(200))
    session.add("PUT", "/schemas/system", FakeResponse(401, "Unauthorized"))
    with pytest.raises(bs.BootstrapError, match="aborted"):
        bs.run(ENV, session, sleep, dest=schemas_tree, ready_file=str(tmp_path / "ready"))


def test_run_does_not_fetch_without_a_package_version(schemas_tree, tmp_path, no_sleep):
    sleep, _ = no_sleep
    calls = []
    bs.run(
        ENV,
        happy_session(),
        sleep,
        dest=schemas_tree,
        ready_file=str(tmp_path / "ready"),
        fetcher=lambda *args, **kwargs: calls.append(args),
    )
    assert calls == []


# ---------------------------------------------------------------- package override


def test_override_fetches_the_requested_version(tmp_path):
    calls = []

    def fetcher(version, dest, **kwargs):
        calls.append((version, dest, kwargs))

    env = dict(
        ENV,
        SCHEMA_PACKAGE_VERSION="v0.30.2",
        SCHEMA_PACKAGE_URL="https://mirror/generic",
        SCHEMA_PACKAGE_TOKEN="t",
        SCHEMA_PACKAGE_SHA256="abc",
    )
    bs.prepare_schemas(env, tmp_path, fetcher)
    assert calls == [
        ("v0.30.2", tmp_path, {"url": "https://mirror/generic", "token": "t", "expected_sha256": "abc"})
    ]


def failing_fetcher(*args, **kwargs):
    raise bs.SchemaFetchError("download failed")


def test_override_failure_fails_the_bootstrap_by_default(tmp_path):
    with pytest.raises(bs.BootstrapError, match="Cannot use schema package v1"):
        bs.prepare_schemas(dict(ENV, SCHEMA_PACKAGE_VERSION="v1"), tmp_path, failing_fetcher)


def test_override_failure_can_fall_back_to_the_bundled_schemas(tmp_path, capsys):
    env = dict(ENV, SCHEMA_PACKAGE_VERSION="v1", SCHEMA_PACKAGE_FALLBACK="TRUE")
    bs.prepare_schemas(env, tmp_path, failing_fetcher)
    assert "WARNING: cannot use schema package v1" in capsys.readouterr().out


def test_active_manifest_is_logged(tmp_path, capsys):
    folder = tmp_path / "shared-schemas"
    folder.mkdir()
    (folder / "manifest.json").write_text(
        json.dumps({"version": "0.30.0-release", "commit": "cafe"})
    )
    bs.prepare_schemas(ENV, tmp_path)
    assert "Schema package in use: version 0.30.0-release, commit cafe" in capsys.readouterr().out


def test_missing_manifest_only_warns(tmp_path, capsys):
    bs.prepare_schemas(ENV, tmp_path)
    assert "no manifest found" in capsys.readouterr().out


# ---------------------------------------------------------------- workers


@pytest.mark.parametrize(
    "env,expected",
    [({}, 1), ({"BOOTSTRAP_WORKERS": ""}, 1), ({"BOOTSTRAP_WORKERS": " 4 "}, 4), ({"BOOTSTRAP_WORKERS": "8"}, 8)],
)
def test_parse_workers(env, expected):
    assert bs.parse_workers(env) == expected


@pytest.mark.parametrize("value", ["0", "-2", "9", "16", "many", "1.5"])
def test_parse_workers_rejects_invalid_values(value):
    with pytest.raises(bs.BootstrapError, match="BOOTSTRAP_WORKERS"):
        bs.parse_workers({"BOOTSTRAP_WORKERS": value})


def test_run_passes_the_worker_count_and_no_shared_session_when_parallel(schemas_tree, tmp_path, no_sleep, monkeypatch):
    sleep, _ = no_sleep
    captured = {}

    def fake_deploy(*args, **kwargs):
        captured.update(kwargs)
        return []

    monkeypatch.setattr(bs, "deploy_shared_schemas", fake_deploy)
    bs.run(dict(ENV, BOOTSTRAP_WORKERS="4"), happy_session(), sleep, dest=schemas_tree, ready_file=str(tmp_path / "r"))
    assert captured["workers"] == 4 and captured["session"] is None

    session = happy_session()
    bs.run(ENV, session, sleep, dest=schemas_tree, ready_file=str(tmp_path / "r2"))
    assert captured["workers"] == 1 and captured["session"] is session


def test_run_rejects_an_invalid_worker_count_before_doing_anything(schemas_tree, tmp_path, no_sleep):
    sleep, _ = no_sleep
    session = FakeSession()
    with pytest.raises(bs.BootstrapError, match="BOOTSTRAP_WORKERS"):
        bs.run(dict(ENV, BOOTSTRAP_WORKERS="0"), session, sleep, dest=schemas_tree, ready_file=str(tmp_path / "r"))
    assert session.calls == []


# ---------------------------------------------------------------- authority


@pytest.mark.parametrize(
    "env,expected",
    [({}, "osdu"), ({"SCHEMA_AUTHORITY": ""}, "osdu"), ({"SCHEMA_AUTHORITY": " acme "}, "acme"),
     ({"SCHEMA_AUTHORITY": "my-company_2"}, "my-company_2")],
)
def test_parse_authority(env, expected):
    assert bs.parse_authority(env) == expected


@pytest.mark.parametrize("value", ["a.b", "a:b", "a b", "a/b", '"x"'])
def test_parse_authority_rejects_invalid_values(value):
    with pytest.raises(bs.BootstrapError, match="SCHEMA_AUTHORITY.*Invalid schema authority"):
        bs.parse_authority({"SCHEMA_AUTHORITY": value})


def test_run_registers_under_the_configured_authority(schemas_tree, tmp_path, no_sleep, capsys):
    sleep, _ = no_sleep
    session = happy_session()
    bs.run(dict(ENV, SCHEMA_AUTHORITY="acme"), session, sleep, dest=schemas_tree, ready_file=str(tmp_path / "r"))
    puts = [call for call in session.calls if call[0] == "PUT"]
    assert puts and all(
        json.loads(call[2]["data"])["schemaInfo"]["schemaIdentity"]["authority"] == "acme" for call in puts
    )
    assert "under authority 'acme'" in capsys.readouterr().out


def test_run_uses_osdu_when_no_authority_is_configured(schemas_tree, tmp_path, no_sleep, capsys):
    sleep, _ = no_sleep
    session = happy_session()
    bs.run(ENV, session, sleep, dest=schemas_tree, ready_file=str(tmp_path / "r"))
    put = next(call for call in session.calls if call[0] == "PUT")
    assert json.loads(put[2]["data"])["schemaInfo"]["schemaIdentity"]["authority"] == "osdu"
    assert "under authority 'osdu'" in capsys.readouterr().out


def test_run_rejects_an_invalid_authority_before_doing_anything(schemas_tree, tmp_path, no_sleep):
    sleep, _ = no_sleep
    session = FakeSession()
    with pytest.raises(bs.BootstrapError, match="SCHEMA_AUTHORITY"):
        bs.run(dict(ENV, SCHEMA_AUTHORITY="a.b"), session, sleep, dest=schemas_tree, ready_file=str(tmp_path / "r"))
    assert session.calls == []


# ---------------------------------------------------------------- main


def test_main_returns_1_on_bootstrap_errors(monkeypatch, capsys):
    for key in bs.REQUIRED_ENV:
        monkeypatch.delenv(key, raising=False)
    assert bs.main([]) == 1
    assert "ERROR" in capsys.readouterr().err


def test_main_waits_until_stopped_after_a_successful_run(monkeypatch):
    called = []
    monkeypatch.setattr(bs, "run", lambda env, **kwargs: called.append(("run", kwargs["dest"])))
    monkeypatch.setattr(bs, "wait_until_stopped", lambda: called.append("wait"))
    monkeypatch.delenv("BOOTSTRAP_EXIT_WHEN_DONE", raising=False)
    monkeypatch.delenv("BOOTSTRAP_HOME", raising=False)
    assert bs.main([]) == 0
    assert called == [("run", Path("/opt")), "wait"]


def test_main_exits_without_waiting_with_exit_when_done(monkeypatch, tmp_path):
    called = []
    monkeypatch.setattr(bs, "run", lambda env, **kwargs: called.append(("run", kwargs["dest"])))
    monkeypatch.setattr(bs, "wait_until_stopped", lambda: called.append("wait"))
    assert bs.main(["--home", str(tmp_path), "--exit-when-done"]) == 0
    assert called == [("run", tmp_path)]


def test_main_does_not_wait_after_a_failure_with_exit_when_done(monkeypatch):
    for key in bs.REQUIRED_ENV:
        monkeypatch.delenv(key, raising=False)
    monkeypatch.setattr(bs, "wait_until_stopped", lambda: pytest.fail("must not wait"))
    assert bs.main(["--exit-when-done"]) == 1


def test_parse_args_defaults_and_environment():
    args = bs.parse_args([], {})
    assert args.home == "/opt" and args.exit_when_done is False
    args = bs.parse_args([], {"BOOTSTRAP_HOME": "/srv/bootstrap", "BOOTSTRAP_EXIT_WHEN_DONE": "TRUE"})
    assert args.home == "/srv/bootstrap" and args.exit_when_done is True
    args = bs.parse_args(["--home", "/x"], {"BOOTSTRAP_HOME": "/srv/bootstrap"})
    assert args.home == "/x"


def test_add_bundled_libs_prepends_an_existing_libs_folder(tmp_path, monkeypatch):
    monkeypatch.setattr(sys, "path", list(sys.path))
    bs.add_bundled_libs(tmp_path)  # no libs folder: nothing changes
    assert str(tmp_path / "libs") not in sys.path
    (tmp_path / "libs").mkdir()
    bs.add_bundled_libs(tmp_path)
    bs.add_bundled_libs(tmp_path)  # idempotent
    assert sys.path[0] == str(tmp_path / "libs")
    assert sys.path.count(str(tmp_path / "libs")) == 1


def test_registration_error_type_is_translated(schemas_tree, tmp_path, no_sleep, monkeypatch):
    sleep, _ = no_sleep
    monkeypatch.setattr(
        bs,
        "deploy_shared_schemas",
        lambda *a, **k: (_ for _ in ()).throw(bs.SchemaRegistrationError("bad input")),
    )
    with pytest.raises(bs.BootstrapError, match="bad input"):
        bs.run(ENV, happy_session(), sleep, dest=schemas_tree, ready_file=str(tmp_path / "ready"))
