"""Registering the schemas: one after another, then in parallel (the plan and the thread pool)."""
import json
import re
import threading
import time
from pathlib import Path

import pytest
import requests

import bootstrap_schema as dss
from conftest import FakeResponse, FakeSession

URL = "http://schema/api/schema-service/v1/schemas/system"


# ---------------------------------------------------------------- sequential registration


def error_body(message):
    return json.dumps({"error": {"code": 400, "message": message}})


def deploy(tree, session, **kwargs):
    kwargs.setdefault("schemas_root", str(tree))
    return dss.deploy_shared_schemas(URL, "Bearer token", session=session, **kwargs)


def test_entry_kind_is_built_from_the_schema_identity():
    payload = json.dumps(
        {
            "schemaInfo": {
                "schemaIdentity": {
                    "authority": "osdu",
                    "source": "wks",
                    "entityType": "work-product-component--WellLog",
                    "schemaVersionMajor": 1,
                    "schemaVersionMinor": 2,
                    "schemaVersionPatch": 3,
                }
            }
        }
    )
    entry = dss.make_entry(payload)
    assert entry.kind == "osdu:wks:work-product-component--WellLog:1.2.3"
    assert entry.entity_type == "work-product-component--WellLog"


def test_invalid_schema_json_is_refused():
    with pytest.raises(dss.SchemaRegistrationError, match="Invalid JSON"):
        dss.make_entry("{not json")


def test_registers_every_schema_with_authority_replaced(schemas_tree, capsys):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(201, "{}"))

    assert deploy(schemas_tree, session) == []

    assert len(session.calls) == 2
    method, url, kwargs = session.calls[0]
    assert (method, url) == ("PUT", URL)
    assert kwargs["headers"] == {"Content-Type": "application/json", "Authorization": "Bearer token"}
    sent = json.loads(kwargs["data"])
    assert sent["schemaInfo"]["schemaIdentity"]["authority"] == "osdu"
    out = capsys.readouterr().out
    assert "The kind osdu:wks:AbstractA:1.0.0 was registered successfully." in out
    assert "All 2 schemas registered, updated or left unchanged because of status PUBLISHED." in out
    assert re.search(r"^This update took \d+\.\d{2} seconds\.$", out, re.M)
    assert "Running time" not in out


def test_schemas_are_registered_under_osdu_by_default(schemas_tree):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(200, "{}"))
    deploy(schemas_tree, session)
    identity = json.loads(session.calls[0][2]["data"])["schemaInfo"]["schemaIdentity"]
    assert identity["authority"] == "osdu"


@pytest.mark.parametrize("authority", ["custom", "my-company_2", "ACME"])
def test_custom_authority_replaces_the_placeholder_everywhere(schemas_tree, authority):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(200, "{}"))
    deploy(schemas_tree, session, authority=authority)
    for call in session.calls:
        payload = call[2]["data"].decode()
        assert dss.SCHEMA_AUTHORITY_TO_REPLACE not in payload
        assert json.loads(payload)["schemaInfo"]["schemaIdentity"]["authority"] == authority


@pytest.mark.parametrize("authority,expected", [(None, "osdu"), ("", "osdu"), ("  ", "osdu"), (" acme ", "acme")])
def test_validate_authority_defaults_and_trims(authority, expected):
    assert dss.validate_authority(authority) == expected


@pytest.mark.parametrize("authority", ["a.b", "a:b", "a b", 'a"b', "a\nb", "a/b", "ä", "{{schema-authority}}"])
def test_invalid_authority_is_refused_before_anything_is_sent(schemas_tree, authority):
    session = FakeSession()
    with pytest.raises(dss.SchemaRegistrationError, match="Invalid schema authority"):
        deploy(schemas_tree, session, authority=authority)
    assert session.calls == []


def test_existing_schema_is_retried_once(schemas_tree, capsys):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(400, error_body(dss.SCHEMA_EXISTS)), FakeResponse(200, "{}"))
    assert deploy(schemas_tree, session) == []
    assert len(session.calls) == 3  # AbstractA twice, AbstractB once


def test_published_schema_is_not_an_error(schemas_tree, capsys):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(400, error_body(dss.PUBLISHED_SCHEMA_ERROR[0])))
    assert deploy(schemas_tree, session) == []
    assert "was already registered with status PUBLISHED" in capsys.readouterr().out


def test_misspelled_published_message_is_also_accepted(schemas_tree):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(400, error_body(dss.PUBLISHED_SCHEMA_ERROR[1])))
    assert deploy(schemas_tree, session) == []


def test_other_errors_are_collected_and_reported(schemas_tree, capsys):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(400, error_body("Invalid schema")))
    failed = deploy(schemas_tree, session)
    assert len(failed) == 2
    assert failed[0] == "Error with kind osdu:wks:AbstractA:1.0.0: Message: Invalid schema"
    assert "Following schemas failed:" in capsys.readouterr().out


def test_internal_error_is_retried_when_not_exiting_early(schemas_tree):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(500, error_body(dss.TRY_AGAIN)), FakeResponse(200, "{}"))
    assert deploy(schemas_tree, session) == []


def test_internal_error_aborts_with_early_exit(schemas_tree):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(500, error_body(dss.TRY_AGAIN)))
    with pytest.raises(dss.SchemaRegistrationError):
        deploy(schemas_tree, session, early_exit=True)
    assert len(session.calls) == 1


def test_unparseable_error_aborts_with_early_exit(schemas_tree):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(401, "Unauthorized"))
    with pytest.raises(dss.SchemaRegistrationError):
        deploy(schemas_tree, session, early_exit=True)


def test_unparseable_error_is_collected_without_early_exit(schemas_tree):
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(401, "Unauthorized"))
    assert len(deploy(schemas_tree, session)) == 2


def test_connection_error_aborts_early_or_is_collected(schemas_tree):
    session = FakeSession()
    session.add("PUT", URL, requests.ConnectionError("boom"))
    with pytest.raises(dss.SchemaRegistrationError, match="Request failed"):
        deploy(schemas_tree, session, early_exit=True)
    assert len(deploy(schemas_tree, session)) == 2


def test_missing_schema_file_is_reported(schemas_tree):
    (schemas_tree / "shared-schemas/osdu/abstract/AbstractB.1.0.0.json").unlink()
    session = FakeSession()
    session.add("PUT", URL, FakeResponse(200, "{}"))
    with pytest.raises(dss.SchemaRegistrationError, match="not found"):
        deploy(schemas_tree, session)


def test_missing_load_sequence_is_reported(schemas_tree):
    (schemas_tree / "shared-schemas/osdu/load_sequence.1.0.0.json").unlink()
    with pytest.raises(dss.SchemaRegistrationError, match="not found"):
        deploy(schemas_tree, FakeSession())


@pytest.mark.parametrize("url,token", [("", "Bearer t"), ("http://x", "")])
def test_url_and_token_are_required(url, token):
    with pytest.raises(dss.SchemaRegistrationError):
        dss.deploy_shared_schemas(url, token)


# ---------------------------------------------------------------- parallel registration


def schema_text(entity, version="1.0.0", refs=()):
    major, minor, patch = (int(part) for part in version.split("."))
    body = {"allOf": [{"$ref": "{{schema-authority}}:wks:%s.json" % ref} for ref in refs]}
    return json.dumps(
        {
            "schemaInfo": {
                "schemaIdentity": {
                    "authority": "{{schema-authority}}",
                    "source": "wks",
                    "entityType": entity,
                    "schemaVersionMajor": major,
                    "schemaVersionMinor": minor,
                    "schemaVersionPatch": patch,
                }
            },
            "schema": body,
        }
    )


def entry(entity, version="1.0.0", refs=()):
    return dss.make_entry(schema_text(entity, version, refs).replace("{{schema-authority}}", "osdu"))


def write_tree(root, specs):
    """specs: (entity, version, [refs like 'Entity:1.0.0']) in load-sequence order."""
    osdu = root / "shared-schemas" / "osdu" / "all"
    osdu.mkdir(parents=True)
    sequence = []
    for entity, version, refs in specs:
        path = osdu / "{}.{}.json".format(entity, version)
        path.write_text(schema_text(entity, version, refs), encoding="utf-8")
        sequence.append(
            {
                "kind": "{{schema-authority}}:wks:%s:%s" % (entity, version),
                "relativePath": "shared-schemas/osdu/all/{}.{}.json".format(entity, version),
            }
        )
    (root / "shared-schemas" / "osdu" / "load_sequence.1.0.0.json").write_text(json.dumps(sequence))
    return root


def standard_specs(chains=8):
    specs = [("AbstractA", "1.0.0", []), ("AbstractB", "1.0.0", ["AbstractA:1.0.0"])]
    for number in range(chains):
        name = "Item{}".format(number)
        specs.append((name, "1.0.0", ["AbstractA:1.0.0", "AbstractB:1.0.0"]))
        specs.append((name, "1.1.0", ["AbstractB:1.0.0"]))
        specs.append((name, "1.2.0", []))
    return specs


class RecordingSession:
    """Accepts every PUT, records start/end events and the highest concurrency."""

    def __init__(self, delay=0.01, fail_kinds=()):
        self.delay = delay
        self.fail_kinds = set(fail_kinds)
        self.events = []
        self.in_flight = 0
        self.max_in_flight = 0
        self._lock = threading.Lock()

    def request(self, method, url, **kwargs):
        identity = json.loads(kwargs["data"])["schemaInfo"]["schemaIdentity"]
        kind = "{}:{}".format(identity["entityType"], "{schemaVersionMajor}.{schemaVersionMinor}.{schemaVersionPatch}".format(**identity))
        with self._lock:
            self.events.append(("start", kind))
            self.in_flight += 1
            self.max_in_flight = max(self.max_in_flight, self.in_flight)
        time.sleep(self.delay)
        with self._lock:
            self.in_flight -= 1
            self.events.append(("end", kind))
        if kind in self.fail_kinds:
            return FakeResponse(401, "Unauthorized")
        return FakeResponse(200, "{}")

    def index(self, what, kind):
        return self.events.index((what, kind))

    @property
    def kinds(self):
        return [kind for what, kind in self.events if what == "start"]


def deploy_parallel(tree, session, **kwargs):
    return dss.deploy_shared_schemas(URL, "Bearer t", session=session, schemas_root=str(tree), **kwargs)


# ---------------------------------------------------------------- planning


def test_plan_separates_abstract_schemas_and_chains_by_entity():
    entries = [
        entry("AbstractA"),
        entry("AbstractB", refs=["AbstractA:1.0.0"]),
        entry("Well", "1.0.0", ["AbstractB:1.0.0"]),
        entry("Wellbore", "1.0.0", ["AbstractB:1.0.0"]),
        entry("Well", "1.1.0", ["AbstractB:1.0.0"]),
    ]
    abstract, chains = dss.plan_parallel(entries)
    assert abstract == [0, 1]
    assert sorted(chains) == [[2, 4], [3]]  # versions of Well stay together, in sequence order


def test_plan_merges_entities_that_refer_to_each_other():
    entries = [
        entry("AbstractA"),
        entry("ManifestA", refs=["AbstractA:1.0.0"]),
        entry("ManifestB", refs=["ManifestA:1.0.0"]),
        entry("Other", refs=["AbstractA:1.0.0"]),
    ]
    _, chains = dss.plan_parallel(entries)
    assert sorted(chains) == [[1, 2], [3]]


def test_plan_keeps_the_load_sequence_order_inside_a_chain():
    entries = [entry("W", "1.1.0"), entry("W", "1.0.0"), entry("X")]
    _, chains = dss.plan_parallel(entries)
    assert sorted(chains) == [[0, 1], [2]]


def test_plan_ignores_references_outside_the_load_sequence():
    assert dss.plan_parallel([entry("Well", refs=["Unknown:1.0.0"])]) == ([], [[0]])


@pytest.mark.parametrize(
    "entries",
    [
        # a schema listed before something it refers to
        [entry("Well", refs=["Other:1.0.0"]), entry("Other")],
        # an abstract schema that refers to a non-abstract one
        [entry("AbstractA", refs=["Well:1.0.0"]), entry("Well")],
        # an abstract schema listed before the abstract schema it refers to
        [entry("AbstractB", refs=["AbstractA:1.0.0"]), entry("AbstractA")],
        # the same kind twice
        [entry("Well"), entry("Well")],
    ],
)
def test_plan_rejects_sequences_that_cannot_be_parallelised(entries):
    assert dss.plan_parallel(entries) is None


def test_plan_holds_for_the_real_shared_schemas():
    root = Path(__file__).resolve().parents[4] / "deployments"
    sequence_file = root / "shared-schemas/osdu/load_sequence.1.0.0.json"
    if not sequence_file.is_file():
        pytest.skip("deployments/shared-schemas is not available")
    entries = []
    for item in json.loads(sequence_file.read_text(encoding="utf-8")):
        text = (root / item["relativePath"]).read_text(encoding="utf-8")
        entries.append(dss.make_entry(text.replace("{{schema-authority}}", "osdu")))

    plan = dss.plan_parallel(entries)

    assert plan is not None, "the shipped load sequence must be parallelisable"
    abstract, chains = plan
    assert all(entries[i].entity_type.startswith("Abstract") for i in abstract)
    assert len(abstract) + sum(len(chain) for chain in chains) == len(entries)
    assert len(chains) > 100
    # every dependency of a chained schema is abstract or earlier in the same chain
    chain_of = {index: number for number, chain in enumerate(chains) for index in chain}
    by_key = {e.key: i for i, e in enumerate(entries)}
    for number, chain in enumerate(chains):
        assert chain == sorted(chain)
        for index in chain:
            for ref in entries[index].refs:
                target = by_key.get(ref)
                if target is not None and target not in abstract:
                    assert chain_of[target] == number and target < index


# ---------------------------------------------------------------- execution


def test_default_registration_is_strictly_sequential(tmp_path):
    specs = standard_specs()
    tree = write_tree(tmp_path, specs)
    session = RecordingSession()

    assert deploy_parallel(tree, session) == []

    assert session.max_in_flight == 1
    assert session.kinds == ["{}:{}".format(entity, version) for entity, version, _ in specs]


def test_parallel_registration_registers_everything_once_and_in_parallel(tmp_path, capsys):
    specs = standard_specs(chains=8)
    tree = write_tree(tmp_path, specs)
    session = RecordingSession()

    assert deploy_parallel(tree, session, workers=4) == []

    expected = sorted("{}:{}".format(entity, version) for entity, version, _ in specs)
    assert sorted(session.kinds) == expected
    assert 2 <= session.max_in_flight <= 4
    assert "in 8 chains with 4 workers" in capsys.readouterr().out


def test_parallel_registration_keeps_dependencies_satisfied(tmp_path):
    tree = write_tree(tmp_path, standard_specs(chains=8))
    session = RecordingSession()
    deploy_parallel(tree, session, workers=4)

    # all abstract schemas finish before anything else starts
    last_abstract_end = max(session.index("end", k) for k in ("AbstractA:1.0.0", "AbstractB:1.0.0"))
    first_other_start = min(i for i, (what, kind) in enumerate(session.events) if what == "start" and not kind.startswith("Abstract"))
    assert last_abstract_end < first_other_start
    assert session.index("end", "AbstractA:1.0.0") < session.index("start", "AbstractB:1.0.0")
    # the versions of one entity never overlap and stay in order
    for number in range(8):
        name = "Item{}".format(number)
        for older, newer in (("1.0.0", "1.1.0"), ("1.1.0", "1.2.0")):
            assert session.index("end", "{}:{}".format(name, older)) < session.index("start", "{}:{}".format(name, newer))


def test_parallel_registration_collects_failures(tmp_path):
    tree = write_tree(tmp_path, standard_specs(chains=4))
    session = RecordingSession(fail_kinds={"Item1:1.1.0"})
    failed = deploy_parallel(tree, session, workers=3)
    assert len(failed) == 1 and "Item1" in failed[0]


def test_parallel_early_exit_stops_the_run_and_reports_the_error(tmp_path):
    specs = standard_specs(chains=40)
    tree = write_tree(tmp_path, specs)
    session = RecordingSession(fail_kinds={"Item0:1.0.0"})

    with pytest.raises(dss.SchemaRegistrationError):
        deploy_parallel(tree, session, workers=2, early_exit=True)

    assert len(session.kinds) < len(specs)


def test_falls_back_to_sequential_when_the_sequence_is_not_safe(tmp_path, capsys):
    specs = [("Well", "1.0.0", ["Other:1.0.0"]), ("Other", "1.0.0", []), ("Third", "1.0.0", [])]
    tree = write_tree(tmp_path, specs)
    session = RecordingSession()

    assert deploy_parallel(tree, session, workers=4) == []

    assert session.max_in_flight == 1
    assert session.kinds == ["Well:1.0.0", "Other:1.0.0", "Third:1.0.0"]
    assert "cannot be parallelised safely" in capsys.readouterr().out


def test_output_lines_of_one_schema_stay_together(tmp_path, capsys):
    tree = write_tree(tmp_path, standard_specs(chains=6))
    deploy_parallel(tree, RecordingSession(), workers=4)
    lines = capsys.readouterr().out.splitlines()
    for position, line in enumerate(lines):
        if line.startswith("Try PUT for id:"):
            kind = line.split("id: ")[1]
            assert lines[position + 1] == "The kind {} was registered successfully.".format(kind)


@pytest.mark.parametrize("workers", [0, -1, dss.MAX_WORKERS + 1])
def test_workers_must_be_in_range(tmp_path, workers):
    with pytest.raises(dss.SchemaRegistrationError, match="workers"):
        deploy_parallel(write_tree(tmp_path, standard_specs(1)), FakeSession(), workers=workers)


def test_parallel_registration_uses_one_session_per_thread_when_none_is_given(tmp_path, monkeypatch):
    sessions = []

    class Recorded(RecordingSession):
        def __init__(self):
            super().__init__()
            sessions.append(self)

    monkeypatch.setattr(dss.requests, "Session", Recorded)
    tree = write_tree(tmp_path, standard_specs(chains=8))

    assert dss.deploy_shared_schemas(URL, "Bearer t", schemas_root=str(tree), workers=4) == []

    assert 2 <= len(sessions) <= 5  # the calling thread plus up to four workers
    assert sum(len(s.kinds) for s in sessions) == len(standard_specs(chains=8))
