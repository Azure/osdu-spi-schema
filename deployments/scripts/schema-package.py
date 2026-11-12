#!/usr/bin/env python3
"""Build the schema bootstrap bundle and, for a release, publish it to the GitLab Generic Package Registry.

Run from the repository root (the schema-package CI job does). SCHEMA_PUBLISH=release uploads the bundle as
X.Y.Z, X.Y and X; SCHEMA_PUBLISH=check (every other pipeline, merge requests included) uploads nothing but runs the
same comparison; anything else only builds it. An alias is skipped when a newer X.Y.Z of its series is published.
A published X.Y.Z always means one set of schemas: when it exists with different schemas the job fails before
uploading anything and VERSION has to be bumped; when it exists with the same schemas it is not uploaded again.
The check mode fails in the same case, so the problem shows in the merge request and not after the merge.
"""
import hashlib
import io
import json
import os
import re
import shutil
import subprocess
import sys
import tarfile
import urllib.error
import urllib.request
from pathlib import Path

PACKAGE = "schema-bootstrap"
BUNDLE = "schema-bootstrap-bundle.tar.gz"
SCHEMAS = Path("deployments/shared-schemas")
MODULE = Path(os.environ.get("BOOTSTRAP_MODULE_DIR", "devops/core-plus/bootstrap-osdu-module"))
OUT = Path("dist/schema-package")
SEMVER = r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)"


def read_version():
    version = (SCHEMAS / "VERSION").read_text().strip()
    if not re.fullmatch(SEMVER, version):
        sys.exit(f"ERROR: {SCHEMAS}/VERSION must be MAJOR.MINOR.PATCH (for example 28.0.0), got '{version}'")
    return version


def build(version, api):
    top = Path(f"schema-bootstrap-{version}")
    shutil.rmtree(top, ignore_errors=True)
    (top / "shared-schemas").mkdir(parents=True)
    for py in MODULE.glob("*.py"):
        shutil.copy2(py, top)
    subprocess.run(
        [sys.executable, "-m", "pip", "install", "--quiet", "--no-compile", "--no-cache-dir",
         "--disable-pip-version-check", "--root-user-action=ignore", "--only-binary=:all:", "--platform", "any",
         "--target", str(top / "libs"), "-r", str(MODULE / "requirements.txt")],
        check=True,
    )
    for src in [*SCHEMAS.rglob("*.json"), SCHEMAS / "VERSION"]:
        dst = top / "shared-schemas" / src.relative_to(SCHEMAS)
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
    manifest = {"version": version, "commit": os.environ.get("CI_COMMIT_SHA", ""),
                "registryUrl": f"{api}/generic" if api else ""}
    (top / "shared-schemas" / "manifest.json").write_text(json.dumps(manifest) + "\n")
    OUT.mkdir(parents=True, exist_ok=True)
    with tarfile.open(OUT / BUNDLE, "w:gz") as tar:
        tar.add(top)
    return OUT / BUNDLE


def call(url, data=None):
    headers = {"JOB-TOKEN": os.environ["CI_JOB_TOKEN"]}
    request = urllib.request.Request(url, data, headers, method="PUT" if data else "GET")
    with urllib.request.urlopen(request, timeout=300) as response:
        return response.read()


def schemas_digest(data):
    """sha256 over the schemas of a bundle: the sorted paths below shared-schemas/ with the sha256 of each file.

    The code, the libraries and manifest.json (it holds the commit) do not count, so two builds of the same schemas
    have the same digest even though their archives differ byte for byte.
    """
    files = {}
    with tarfile.open(fileobj=io.BytesIO(data), mode="r:gz") as tar:
        for member in tar:
            parts = Path(member.name).parts
            if not member.isreg() or len(parts) < 3 or parts[1] != "shared-schemas":
                continue
            relative = Path(*parts[2:])
            if relative != Path("manifest.json"):
                files[relative.as_posix()] = hashlib.sha256(tar.extractfile(member).read()).hexdigest()
    return hashlib.sha256("".join(f"{path} {digest}\n" for path, digest in sorted(files.items())).encode()).hexdigest()


def published_digest(api, version):
    """The schemas digest of the published bundle `version`, or None when the package has no bundle file."""
    try:
        return schemas_digest(call(f"{api}/generic/{PACKAGE}/{version}/{BUNDLE}"))
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return None
        raise


def published_versions(api):
    listing, page = [], 1
    while batch := json.loads(call(f"{api}?package_type=generic&package_name={PACKAGE}&per_page=100&page={page}")):
        listing, page = listing + batch, page + 1
    return {p["version"] for p in listing if p["name"] == PACKAGE}


def same_as_published(version, api, existing, data):
    """True when `version` is published with the schemas of `data`, False when it is not published (or has no
    bundle file). Exits with an error when it is published with different schemas: X.Y.Z always means one set."""
    if version not in existing:
        return False
    published, current = published_digest(api, version), schemas_digest(data)
    if published is not None and published != current:
        v = tuple(map(int, version.split(".")))
        sys.exit(f"ERROR: {version} is already published with different schemas (published {published[:12]}, "
                 f"this build {current[:12]}). A published version does not change: bump "
                 f"{SCHEMAS}/VERSION (for example to {v[0]}.{v[1]}.{v[2] + 1}) and run the pipeline again.")
    return published is not None


def check(version, api, bundle):
    """Merge request check: fail when `version` is already published with different schemas; upload nothing."""
    data = bundle.read_bytes()
    sha = schemas_digest(data)[:12]
    if same_as_published(version, api, published_versions(api), data):
        print(f"OK        {version}: already published with the same schemas (sha256 {sha}), nothing to do")
    else:
        print(f"OK        {version}: not published yet, nothing to compare (schemas sha256 {sha})")


def publish(version, api, bundle):
    existing = published_versions(api)
    released = [tuple(map(int, v.split("."))) for v in existing if re.fullmatch(SEMVER, v)]
    v = tuple(map(int, version.split(".")))
    data = bundle.read_bytes()
    # decide before anything is uploaded, so a failure leaves the aliases as they are
    same = same_as_published(version, api, existing, data)
    for name, n in ((version, 3), (f"{v[0]}.{v[1]}", 2), (str(v[0]), 1)):
        newer = max((r for r in released if r[:n] == v[:n] and r > v), default=None)
        if newer:
            print(f"SKIP      {name}: {'.'.join(map(str, newer))} is newer than {version}, not changed")
            continue
        if name == version and same:
            print(f"SAME      {name}: already published with the same schemas, not uploaded again")
            continue
        print(f"{'OVERWRITE' if name != version and name in existing else 'NEW      '} {name} with {version}")
        call(f"{api}/generic/{PACKAGE}/{name}/{BUNDLE}", data)


def main():
    version = read_version()
    mode = os.environ.get("SCHEMA_PUBLISH", "none")
    print(f"VERSION={version}, mode {mode}")
    api = ""
    if os.environ.get("CI_API_V4_URL"):
        api = f"{os.environ['CI_API_V4_URL']}/projects/{os.environ['CI_PROJECT_ID']}/packages"
    bundle = build(version, api)
    if mode == "release":
        publish(version, api, bundle)
    elif mode == "check":
        if not api:
            sys.exit("ERROR: SCHEMA_PUBLISH=check needs the GitLab API (CI_API_V4_URL and CI_PROJECT_ID)")
        check(version, api, bundle)
    else:
        print(f"SCHEMA_PUBLISH is neither release nor check (pipeline source "
              f"'{os.environ.get('CI_PIPELINE_SOURCE', '')}', branch '{os.environ.get('CI_COMMIT_BRANCH', '')}'): "
              "the bundle is built only, nothing is published and nothing is compared")


if __name__ == "__main__":
    main()
