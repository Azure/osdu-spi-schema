"""The bundle: pinned libraries, extracting the schemas, the sha256 pin and downloading another version."""
import io
import json
import re
import tarfile
from pathlib import Path

import pytest

import bootstrap_schema as bs
from conftest import FakeResponse, FakeSession

MODULE_DIR = Path(__file__).resolve().parents[1]

SCHEMAS = {
    "osdu/abstract/AbstractA.1.0.0.json": '{"a": 1}',
    "osdu/abstract/AbstractB.1.0.0.json": '{"b": 2}',
    "osdu/load_sequence.1.0.0.json": "[]",
}
REGISTRY = "https://gitlab.test/api/v4/projects/42/packages/generic"


# ---------------------------------------------------------------- helpers


def make_bundle(tmp_path, name="bundle", schemas=None, libs="libs v1", top="schema-bootstrap-v1"):
    """Write a valid bundle ``<tmp_path>/<name>/schema-bootstrap-bundle.tar.gz``; return the file and its manifest."""
    schemas = SCHEMAS if schemas is None else schemas
    folder = tmp_path / name
    folder.mkdir(parents=True)
    manifest = {"version": "v1", "commit": "abc1234deadbeef", "registryUrl": REGISTRY}
    members = {
        "bootstrap_schema.py": "# code",
        "libs/requests/__init__.py": libs,
        "shared-schemas/manifest.json": json.dumps(manifest),
    }
    members.update({"shared-schemas/" + key: value for key, value in schemas.items()})
    archive = folder / bs.BUNDLE_NAME
    with tarfile.open(archive, "w:gz") as tar:
        for member_name, text in sorted(members.items()):
            data = text.encode()
            info = tarfile.TarInfo("{}/{}".format(top, member_name))
            info.size = len(data)
            tar.addfile(info, io.BytesIO(data))
    return archive, manifest


def write_tar(path, entries):
    with tarfile.open(path, "w:gz") as tar:
        for name, data in entries:
            info = tarfile.TarInfo(name)
            info.size = len(data)
            tar.addfile(info, io.BytesIO(data))
    return path


# ---------------------------------------------------------------- pinned libraries


def test_requirements_pin_every_library_with_a_hash():
    text = (MODULE_DIR / "requirements.txt").read_text()
    lines = [line for line in text.replace("\\\n", " ").splitlines() if line.strip() and not line.startswith("#")]
    names = set()
    for line in lines:
        match = re.match(r"^([A-Za-z0-9._-]+)==([0-9][^\s;]*)\s+--hash=sha256:[0-9a-f]{64}(\s+--hash=sha256:[0-9a-f]{64})*$", line)
        assert match, "not an exact pin with a sha256 hash: {!r}".format(line)
        names.add(match.group(1).lower())
    # requests and everything it needs at run time
    assert names == {"requests", "urllib3", "idna", "certifi", "charset-normalizer"}


def test_dev_requirements_do_not_mix_with_the_hashed_runtime_requirements():
    # pip would demand hashes for the test packages too if both were installed together
    assert "-r requirements.txt" not in (MODULE_DIR / "requirements-dev.txt").read_text()


# ---------------------------------------------------------------- install


def test_install_extracts_only_the_schemas_and_replaces_existing_ones(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    dest = tmp_path / "opt"
    stale = dest / "shared-schemas" / "stale.json"
    stale.parent.mkdir(parents=True)
    stale.write_text("{}")

    bs.install_bundle(bundle, dest)

    assert not stale.exists()
    assert (dest / "shared-schemas/osdu/abstract/AbstractA.1.0.0.json").read_text() == '{"a": 1}'
    assert bs.bundled_manifest(dest)["version"] == "v1"
    assert [path.name for path in dest.iterdir()] == ["shared-schemas"]  # no code, libs or staging leftovers


def test_install_checks_the_pin_against_the_sha256_of_the_bundle_file(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    dest = tmp_path / "opt"
    with pytest.raises(bs.SchemaFetchError, match="Checksum mismatch"):
        bs.install_bundle(bundle, dest, expected_sha256="f" * 64)
    assert not (dest / "shared-schemas").exists()  # nothing was extracted

    pin = bs.sha256_file(bundle)
    bs.install_bundle(bundle, dest, expected_sha256="  {}\n".format(pin.upper()))  # case and blanks do not matter
    assert (dest / "shared-schemas/osdu").is_dir()


def test_install_without_a_pin_does_not_check_the_bundle(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    bs.install_bundle(bundle, tmp_path / "opt", expected_sha256="")
    assert (tmp_path / "opt/shared-schemas/osdu").is_dir()


@pytest.mark.parametrize(
    "name",
    ["../outside.json", "/abs/outside.json", "top/shared-schemas/../../outside.json"],
)
def test_install_rejects_unsafe_paths(tmp_path, name):
    bundle = write_tar(tmp_path / "evil.tar.gz", [(name, b"{}")])
    dest = tmp_path / "opt"
    with pytest.raises(bs.SchemaFetchError, match="Unsafe path"):
        bs.install_bundle(bundle, dest)
    assert not (tmp_path / "outside.json").exists()
    assert not (dest / "shared-schemas").exists()


@pytest.mark.parametrize(
    "entries,message",
    [
        ([("README.txt", b"x")], "outside the top-level folder"),
        ([("top/a.txt", b"x"), ("other/b.txt", b"y")], "more than one top-level folder"),
    ],
)
def test_install_rejects_files_outside_a_single_top_level_folder(tmp_path, entries, message):
    bundle = write_tar(tmp_path / "bad.tar.gz", entries)
    with pytest.raises(bs.SchemaFetchError, match=message):
        bs.install_bundle(bundle, tmp_path / "opt")


def test_install_rejects_links_among_the_schemas(tmp_path):
    bundle = tmp_path / "links.tar.gz"
    with tarfile.open(bundle, "w:gz") as tar:
        info = tarfile.TarInfo("top/shared-schemas/link")
        info.type = tarfile.SYMTYPE
        info.linkname = "/etc/passwd"
        tar.addfile(info)
    with pytest.raises(bs.SchemaFetchError, match="Unsupported entry"):
        bs.install_bundle(bundle, tmp_path / "opt")


def test_install_ignores_entries_that_are_not_schemas(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    extended = tmp_path / "with-link.tar.gz"
    with tarfile.open(bundle) as source, tarfile.open(extended, "w:gz") as target:
        for member in source:
            target.addfile(member, source.extractfile(member) if member.isreg() else None)
        link = tarfile.TarInfo("schema-bootstrap-v1/libs/link")
        link.type = tarfile.SYMTYPE
        link.linkname = "/etc/passwd"
        target.addfile(link)

    dest = tmp_path / "opt"
    bs.install_bundle(extended, dest)  # the link is not extracted, so it cannot do harm

    assert [path.name for path in dest.iterdir()] == ["shared-schemas"]


def test_install_rejects_a_bundle_without_schemas(tmp_path):
    bundle = write_tar(tmp_path / "empty.tar.gz", [("top/bootstrap_schema.py", b"#")])
    with pytest.raises(bs.SchemaFetchError, match="no shared-schemas/ folder"):
        bs.install_bundle(bundle, tmp_path / "opt")


@pytest.mark.parametrize("content", [b"not a gzip file", b""])
def test_install_rejects_a_file_that_is_not_a_bundle(tmp_path, content):
    bundle = tmp_path / "broken.tar.gz"
    bundle.write_bytes(content)
    with pytest.raises(bs.SchemaFetchError, match="Cannot read the bundle"):
        bs.install_bundle(bundle, tmp_path / "opt")


def test_install_rejects_a_truncated_bundle_and_keeps_the_installed_schemas(tmp_path):
    bundle, _ = make_bundle(tmp_path, schemas={"osdu/big.json": "x" * 100_000})
    dest = tmp_path / "opt"
    bs.install_bundle(make_bundle(tmp_path, "good")[0], dest)
    truncated = tmp_path / "truncated.tar.gz"
    truncated.write_bytes(bundle.read_bytes()[:-30])

    with pytest.raises(bs.SchemaFetchError, match="Cannot read the bundle"):
        bs.install_bundle(truncated, dest)

    assert (dest / "shared-schemas/osdu/abstract/AbstractA.1.0.0.json").is_file()
    assert [path.name for path in dest.iterdir()] == ["shared-schemas"]


# ---------------------------------------------------------------- manifest and registry URL


def test_bundled_manifest(tmp_path):
    assert bs.bundled_manifest(tmp_path) is None
    folder = tmp_path / "shared-schemas"
    folder.mkdir()
    (folder / "manifest.json").write_text("not json")
    assert bs.bundled_manifest(tmp_path) is None
    (folder / "manifest.json").write_text(json.dumps({"version": "v"}))
    assert bs.bundled_manifest(tmp_path) == {"version": "v"}


def test_registry_url_priority(tmp_path):
    assert bs.registry_url("", tmp_path) == bs.DEFAULT_REGISTRY_URL
    assert bs.registry_url("https://x.test/g/", tmp_path) == "https://x.test/g"
    bs.install_bundle(make_bundle(tmp_path)[0], tmp_path / "opt")
    assert bs.registry_url("", tmp_path / "opt") == REGISTRY
    assert bs.registry_url("https://x.test/g", tmp_path / "opt") == "https://x.test/g"


# ---------------------------------------------------------------- fetch


class BinaryResponse(FakeResponse):
    def __init__(self, status_code, data):
        super().__init__(status_code, "")
        self.content = data

    def iter_content(self, chunk_size=1):
        yield self.content


def serve(session, bundle, version="v1", base="https://mirror.test/generic"):
    session.add(
        "GET", "{}/schema-bootstrap/{}/{}".format(base, version, bs.BUNDLE_NAME), BinaryResponse(200, bundle.read_bytes())
    )


def test_fetch_downloads_and_installs(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    session = FakeSession()
    serve(session, bundle, "v1.2.3")
    dest = tmp_path / "opt"

    bs.fetch_schemas("v1.2.3", dest, url="https://mirror.test/generic", token="secret", session=session, delay=0)

    assert (dest / "shared-schemas/osdu/abstract/AbstractA.1.0.0.json").is_file()
    assert [path.name for path in dest.iterdir()] == ["shared-schemas"]
    assert [call[1] for call in session.calls] == [
        "https://mirror.test/generic/schema-bootstrap/v1.2.3/schema-bootstrap-bundle.tar.gz"
    ]  # only the bundle, no checksum file
    assert session.calls[0][2]["headers"] == {"PRIVATE-TOKEN": "secret"}


def test_fetch_enforces_the_pin(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    session = FakeSession()
    serve(session, bundle)
    with pytest.raises(bs.SchemaFetchError, match="Checksum mismatch"):
        bs.fetch_schemas("v1", tmp_path / "opt", url="https://mirror.test/generic", expected_sha256="0" * 64,
                         session=session, delay=0)
    assert not (tmp_path / "opt" / "shared-schemas").exists()

    session = FakeSession()
    serve(session, bundle)
    bs.fetch_schemas("v1", tmp_path / "opt", url="https://mirror.test/generic",
                     expected_sha256=bs.sha256_file(bundle), session=session, delay=0)
    assert (tmp_path / "opt/shared-schemas/osdu").is_dir()


def test_fetch_uses_the_registry_url_of_the_bundled_manifest(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    dest = tmp_path / "opt"
    bs.install_bundle(bundle, dest)
    session = FakeSession()
    serve(session, bundle, "0.30.0-release", REGISTRY)

    bs.fetch_schemas("0.30.0-release", dest, session=session, delay=0)

    assert session.calls[0][1].startswith(REGISTRY + "/schema-bootstrap/0.30.0-release/")
    assert session.calls[0][2]["headers"] == {}


def test_fetch_encodes_the_version_in_the_url(tmp_path):
    session = FakeSession()
    session.add("GET", "/schema-bootstrap/feature%2Fx/", BinaryResponse(404, b""))
    with pytest.raises(bs.SchemaFetchError):
        bs.fetch_schemas("feature/x", tmp_path / "opt", url="https://r.test", session=session, delay=0)
    assert "/schema-bootstrap/feature%2Fx/" in session.calls[0][1]


def test_fetch_retries_transient_errors(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    session = FakeSession()
    session.add(
        "GET", "https://r.test/schema-bootstrap/v1/" + bs.BUNDLE_NAME,
        BinaryResponse(503, b""), BinaryResponse(200, bundle.read_bytes()),
    )
    bs.fetch_schemas("v1", tmp_path / "opt", url="https://r.test", session=session, delay=0)
    assert len(session.calls) == 2


def test_fetch_gives_up_after_the_attempts(tmp_path):
    session = FakeSession()
    session.add("GET", bs.BUNDLE_NAME, BinaryResponse(503, b""))
    with pytest.raises(bs.SchemaFetchError, match="failed after 3 attempts"):
        bs.fetch_schemas("v1", tmp_path / "opt", url="https://r.test", session=session, attempts=3, delay=0)
    assert len(session.calls) == 3


@pytest.mark.parametrize("status", [401, 403, 404])
def test_fetch_does_not_retry_client_errors(tmp_path, status):
    session = FakeSession()
    session.add("GET", bs.BUNDLE_NAME, BinaryResponse(status, b""))
    with pytest.raises(bs.SchemaFetchError, match="HTTP {}".format(status)):
        bs.fetch_schemas("nope", tmp_path / "opt", url="https://r.test", session=session, delay=0)
    assert len(session.calls) == 1


def test_fetch_failure_keeps_the_installed_schemas(tmp_path):
    bundle, _ = make_bundle(tmp_path)
    dest = tmp_path / "opt"
    bs.install_bundle(bundle, dest)
    session = FakeSession()
    session.add("GET", "https://r.test/schema-bootstrap/v9/" + bs.BUNDLE_NAME, BinaryResponse(200, b"corrupted"))

    with pytest.raises(bs.SchemaFetchError):
        bs.fetch_schemas("v9", dest, url="https://r.test", session=session, delay=0)

    assert (dest / "shared-schemas/osdu/abstract/AbstractA.1.0.0.json").is_file()
