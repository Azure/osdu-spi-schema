"""deployments/scripts/schema-package.py: the schemas digest and what publish does with a published X.Y.Z."""
import importlib.util
import io
import json
import tarfile
import urllib.error
from pathlib import Path

import pytest

SCRIPT = Path(__file__).resolve().parents[4] / "deployments" / "scripts" / "schema-package.py"
API = "https://gitlab.test/api/v4/projects/42/packages"

spec = importlib.util.spec_from_file_location("schema_package_script", SCRIPT)
sp = importlib.util.module_from_spec(spec)
spec.loader.exec_module(sp)

SCHEMAS = {
    "osdu/abstract/AbstractA.1.0.0.json": '{"a": 1}',
    "osdu/load_sequence.1.0.0.json": "[]",
}


def bundle_bytes(version="27.1.0", schemas=None, commit="abc", code="# code", libs="libs v1", mtime=0):
    """A bundle laid out like build() writes it: schema-bootstrap-<version>/{code,libs,shared-schemas}."""
    schemas = SCHEMAS if schemas is None else schemas
    top = "schema-bootstrap-" + version
    members = {
        "bootstrap_schema.py": code,
        "libs/requests/__init__.py": libs,
        "shared-schemas/VERSION": version + "\n",
        "shared-schemas/manifest.json": json.dumps({"version": version, "commit": commit}),
    }
    members.update({"shared-schemas/" + name: text for name, text in schemas.items()})
    buffer = io.BytesIO()
    with tarfile.open(fileobj=buffer, mode="w:gz") as tar:
        for name, text in sorted(members.items()):
            data = text.encode()
            info = tarfile.TarInfo(top + "/" + name)
            info.size, info.mtime = len(data), mtime
            tar.addfile(info, io.BytesIO(data))
    return buffer.getvalue()


class Registry:
    """Stands in for call(): a package listing, the published bundles and a record of every upload."""

    def __init__(self, published=None, no_file=()):
        self.published = dict(published or {})  # name -> bundle bytes
        self.no_file = set(no_file)  # listed packages without a bundle file
        self.uploads = []

    def __call__(self, url, data=None):
        if data is not None:
            self.uploads.append(url.rsplit("/", 2)[-2])
            return b""
        if url.startswith(API + "?"):
            page = int(url.rsplit("page=", 1)[1])
            names = sorted(set(self.published) | self.no_file)
            return json.dumps([{"name": sp.PACKAGE, "version": n} for n in names] if page == 1 else []).encode()
        name = url.rsplit("/", 2)[-2]
        if name in self.no_file or name not in self.published:
            raise urllib.error.HTTPError(url, 404, "Not Found", {}, None)
        return self.published[name]


def publish(tmp_path, monkeypatch, registry, version="27.1.0", **bundle_args):
    file = tmp_path / "bundle.tar.gz"
    file.write_bytes(bundle_bytes(version, **bundle_args))
    monkeypatch.setattr(sp, "call", registry)
    sp.publish(version, API, file)


# ---------------------------------------------------------------- digest


def test_digest_ignores_code_libraries_manifest_and_archive_bytes():
    base = sp.schemas_digest(bundle_bytes())
    rebuilt = bundle_bytes(commit="def", code="# other code", libs="libs v2", mtime=1234567890)
    assert rebuilt != bundle_bytes()  # the archives differ byte for byte
    assert sp.schemas_digest(rebuilt) == base


@pytest.mark.parametrize(
    "schemas",
    [
        {**SCHEMAS, "osdu/abstract/AbstractA.1.0.0.json": '{"a": 2}'},  # content changed
        {**SCHEMAS, "osdu/new.1.0.0.json": "{}"},  # file added
        {"osdu/abstract/AbstractA.1.0.0.json": '{"a": 1}'},  # file removed
        {"osdu/renamed.json": '{"a": 1}', "osdu/load_sequence.1.0.0.json": "[]"},  # file renamed
    ],
)
def test_digest_changes_with_the_schemas(schemas):
    assert sp.schemas_digest(bundle_bytes(schemas=schemas)) != sp.schemas_digest(bundle_bytes())


def test_digest_counts_a_schema_file_called_manifest_json_in_a_subfolder():
    with_schema = {**SCHEMAS, "osdu/manifest.json": "{}"}
    assert sp.schemas_digest(bundle_bytes(schemas=with_schema)) != sp.schemas_digest(bundle_bytes())


# ---------------------------------------------------------------- publish


def test_publish_uploads_all_three_names_when_nothing_is_published(tmp_path, monkeypatch, capsys):
    registry = Registry()
    publish(tmp_path, monkeypatch, registry)
    assert registry.uploads == ["27.1.0", "27.1", "27"]
    assert capsys.readouterr().out.count("NEW") == 3


def test_publish_fails_before_any_upload_when_the_version_exists_with_other_schemas(tmp_path, monkeypatch):
    registry = Registry({"27.1.0": bundle_bytes(), "27.1": bundle_bytes(), "27": bundle_bytes()})
    changed = {**SCHEMAS, "osdu/abstract/AbstractA.1.0.0.json": '{"a": 2}'}

    with pytest.raises(SystemExit) as error:
        publish(tmp_path, monkeypatch, registry, schemas=changed)

    message = str(error.value)
    assert "27.1.0 is already published with different schemas" in message
    assert "bump" in message and "27.1.1" in message
    assert registry.uploads == []  # the aliases were not touched either


def test_publish_does_not_upload_the_version_again_when_the_schemas_are_the_same(tmp_path, monkeypatch, capsys):
    registry = Registry({"27.1.0": bundle_bytes(commit="old"), "27.1": bundle_bytes(), "27": bundle_bytes()})

    publish(tmp_path, monkeypatch, registry, commit="new")  # a re-run on another commit, same schemas

    assert registry.uploads == ["27.1", "27"]
    out = capsys.readouterr().out
    assert "SAME      27.1.0" in out
    assert "OVERWRITE 27.1 with 27.1.0" in out and "OVERWRITE 27 with 27.1.0" in out


def test_publish_a_new_patch_overwrites_only_the_aliases(tmp_path, monkeypatch, capsys):
    registry = Registry({"27.1.0": bundle_bytes(), "27.1": bundle_bytes(), "27": bundle_bytes()})
    changed = {**SCHEMAS, "osdu/new.1.0.0.json": "{}"}

    publish(tmp_path, monkeypatch, registry, version="27.1.1", schemas=changed)

    assert registry.uploads == ["27.1.1", "27.1", "27"]
    out = capsys.readouterr().out
    assert "NEW       27.1.1" in out
    assert "OVERWRITE 27.1 with 27.1.1" in out


def test_publish_uploads_a_listed_version_that_has_no_bundle_file(tmp_path, monkeypatch):
    registry = Registry(no_file={"27.1.0"})
    publish(tmp_path, monkeypatch, registry)
    assert registry.uploads == ["27.1.0", "27.1", "27"]


def test_publish_a_hotfix_of_an_older_minor_leaves_the_newer_aliases_alone(tmp_path, monkeypatch, capsys):
    registry = Registry({"27.1.0": bundle_bytes(), "27.2.0": bundle_bytes("27.2.0"), "27.1": bundle_bytes(),
                         "27.2": bundle_bytes("27.2.0"), "27": bundle_bytes("27.2.0")})

    publish(tmp_path, monkeypatch, registry, version="27.1.1", schemas={**SCHEMAS, "osdu/fix.json": "{}"})

    assert registry.uploads == ["27.1.1", "27.1"]
    assert "SKIP      27: 27.2.0 is newer than 27.1.1" in capsys.readouterr().out


# ---------------------------------------------------------------- merge request check


def run_check(tmp_path, monkeypatch, registry, version="27.1.0", **bundle_args):
    file = tmp_path / "bundle.tar.gz"
    file.write_bytes(bundle_bytes(version, **bundle_args))
    monkeypatch.setattr(sp, "call", registry)
    sp.check(version, API, file)


def test_check_passes_for_a_version_that_is_not_published(tmp_path, monkeypatch, capsys):
    registry = Registry({"27.0.0": bundle_bytes("27.0.0"), "27.0": bundle_bytes("27.0.0"), "27": bundle_bytes("27.0.0")})
    run_check(tmp_path, monkeypatch, registry, version="27.1.0")
    out = capsys.readouterr().out
    assert "27.1.0: not published yet, nothing to compare" in out
    assert "schemas sha256 " + sp.schemas_digest(bundle_bytes())[:12] in out
    assert registry.uploads == []


def test_check_passes_for_a_version_published_with_the_same_schemas(tmp_path, monkeypatch, capsys):
    registry = Registry({"27.1.0": bundle_bytes(commit="old")})
    run_check(tmp_path, monkeypatch, registry, commit="new")
    out = capsys.readouterr().out
    assert "27.1.0: already published with the same schemas (sha256 " + sp.schemas_digest(bundle_bytes())[:12] in out
    assert "nothing to do" in out
    assert registry.uploads == []


def test_check_fails_for_a_version_published_with_other_schemas(tmp_path, monkeypatch):
    registry = Registry({"27.1.0": bundle_bytes()})
    with pytest.raises(SystemExit, match="27.1.0 is already published with different schemas"):
        run_check(tmp_path, monkeypatch, registry, schemas={**SCHEMAS, "osdu/new.1.0.0.json": "{}"})
    assert registry.uploads == []


def test_check_never_uploads(tmp_path, monkeypatch):
    registry = Registry()
    run_check(tmp_path, monkeypatch, registry)
    assert registry.uploads == []


def test_main_check_mode_needs_the_gitlab_api(monkeypatch):
    monkeypatch.setenv("SCHEMA_PUBLISH", "check")
    monkeypatch.delenv("CI_API_V4_URL", raising=False)
    monkeypatch.setattr(sp, "read_version", lambda: "27.1.0")
    monkeypatch.setattr(sp, "build", lambda version, api: Path("unused"))
    with pytest.raises(SystemExit, match="needs the GitLab API"):
        sp.main()


def test_publish_passes_other_registry_errors_on(tmp_path, monkeypatch):
    class Broken(Registry):
        def __call__(self, url, data=None):
            if data is None and not url.startswith(API + "?"):
                raise urllib.error.HTTPError(url, 500, "Server Error", {}, None)
            return super().__call__(url, data)

    with pytest.raises(urllib.error.HTTPError):
        publish(tmp_path, monkeypatch, Broken({"27.1.0": bundle_bytes()}))
