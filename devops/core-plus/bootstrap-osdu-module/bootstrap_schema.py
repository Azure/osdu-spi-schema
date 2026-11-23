#!/usr/bin/env python3
"""Bootstrap the Schema service with the shared OSDU schemas (core-plus).

Steps:

1. Validate the environment.
2. Optionally replace the bundled schemas with those of a published bundle version
   (``SCHEMA_PACKAGE_VERSION``).
3. Request an access token from the OpenID provider.
4. Wait for the Entitlements and Schema services.
5. Register the shared schemas on the system endpoint.
6. Write the readiness file and keep the container alive until it is stopped.

Expected environment variables:

* DATA_PARTITION, SCHEMA_URL, ENTITLEMENTS_HOST
* OPENID_PROVIDER_URL, OPENID_PROVIDER_CLIENT_ID, OPENID_PROVIDER_CLIENT_SECRET

Optional:

* SCHEMA_PACKAGE_VERSION, SCHEMA_PACKAGE_URL, SCHEMA_PACKAGE_SHA256 (the sha256 of the bundle file, as GitLab
  shows it), SCHEMA_PACKAGE_TOKEN, SCHEMA_PACKAGE_FALLBACK (see README)
* SCHEMA_AUTHORITY: authority the schemas are registered under (default osdu)
* BOOTSTRAP_WORKERS: threads used to register independent schemas (default 1, max 8)
* BOOTSTRAP_HOME / --home: folder that contains shared-schemas/ (default /opt)
* BOOTSTRAP_EXIT_WHEN_DONE / --exit-when-done: exit after registration instead of staying alive

How the schemas are registered (``deploy_shared_schemas()``):

* ``PUT`` is used for every schema; the service creates a missing schema on PUT.
* ``Schema Id is already present`` responses are retried once, ``Internal server error`` responses only without
  ``early_exit``.
* Schemas that are already ``PUBLISHED`` are left alone and are not an error.
* The bootstrap runs with ``early_exit``: the first unexpected error (``Internal server error`` included) aborts
  the run.

Schemas are registered strictly in load-sequence order by default (``BOOTSTRAP_WORKERS=1``). With more than one
worker the load sequence is split into two phases that keep the dependencies satisfied:

1. The ``Abstract*`` schemas, in load-sequence order. Everything else may refer to them, and they refer to each
   other.
2. All other schemas, as independent chains that run in parallel. A chain holds the versions of one entity in
   load-sequence order (the service checks every new version against the previous ones) and also entities that
   refer to each other.

A cheap ``$ref`` scan verifies these assumptions for the package that is being loaded; if they do not hold, the
schemas are registered sequentially instead.
"""
from __future__ import annotations

import argparse
import datetime
import hashlib
import json
import os
import re
import shutil
import signal
import sys
import tarfile
import tempfile
import threading
import time
import zlib
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path, PurePosixPath
from typing import Callable, Dict, List, Mapping, NamedTuple, Optional, Tuple
from urllib.parse import quote


def add_bundled_libs(base: Path) -> None:
    """Use the libraries shipped in ``<base>/libs`` (standalone bundle layout) when that folder exists."""
    libs = base / "libs"
    if libs.is_dir() and str(libs) not in sys.path:
        sys.path.insert(0, str(libs))


add_bundled_libs(Path(__file__).resolve().parent)

import requests  # noqa: E402

REQUIRED_ENV = (
    "DATA_PARTITION",
    "SCHEMA_URL",
    "ENTITLEMENTS_HOST",
    "OPENID_PROVIDER_URL",
    "OPENID_PROVIDER_CLIENT_ID",
    "OPENID_PROVIDER_CLIENT_SECRET",
)

MAX_RETRIES = 60
RETRY_DELAY = 3
TIMEOUT = (5, 15)
ENTITLEMENTS_SETTLE_SECONDS = 5
SCHEMAS_PATH = "/api/schema-service/v1/schemas/system"
INFO_PATH = "/api/schema-service/v1/info"
ENTITLEMENTS_PATH = "/api/entitlements/v2/groups"
DEFAULT_READY_FILE = "/tmp/bootstrap_ready"

DEFAULT_HOME = "/opt"

# The published bundle: <registry>/schema-bootstrap/<version>/schema-bootstrap-bundle.tar.gz
PACKAGE_NAME = "schema-bootstrap"
BUNDLE_NAME = "schema-bootstrap-bundle.tar.gz"
MANIFEST_NAME = "manifest.json"
SCHEMAS_FOLDER = "shared-schemas"
DEFAULT_REGISTRY_URL = (
    "https://community.opengroup.org/api/v4/projects/"
    "osdu%2Fplatform%2Fsystem%2Fschema-service/packages/generic"
)


class BootstrapError(Exception):
    """A fatal bootstrap problem; the container exits with a non-zero code."""


class SchemaFetchError(BootstrapError):
    """The schemas of another bundle version could not be downloaded or are not acceptable."""


def log(message: str, error: bool = False) -> None:
    timestamp = datetime.datetime.now().astimezone().isoformat(timespec="seconds")
    if error:
        print("[{}] ERROR: {}".format(timestamp, message), file=sys.stderr, flush=True)
    else:
        print("[{}] {}".format(timestamp, message), flush=True)


def validate_env(env: Mapping[str, str]) -> None:
    for name in REQUIRED_ENV:
        if not env.get(name):
            raise BootstrapError("{} is missing".format(name))


def parse_workers(env: Mapping[str, str]) -> int:
    """Number of threads for the registration (``BOOTSTRAP_WORKERS``, default 1)."""
    raw = env.get("BOOTSTRAP_WORKERS", "").strip()
    if not raw:
        return 1
    try:
        workers = int(raw)
    except ValueError as error:
        raise BootstrapError("BOOTSTRAP_WORKERS must be an integer, got {!r}".format(raw)) from error
    if not 1 <= workers <= MAX_WORKERS:
        raise BootstrapError(
            "BOOTSTRAP_WORKERS must be between 1 and {}, got {}".format(MAX_WORKERS, workers)
        )
    return workers


def parse_authority(env: Mapping[str, str]) -> str:
    """Authority to register the schemas under (``SCHEMA_AUTHORITY``, default ``osdu``)."""
    try:
        return validate_authority(env.get("SCHEMA_AUTHORITY"))
    except SchemaRegistrationError as error:
        raise BootstrapError("SCHEMA_AUTHORITY: {}".format(error)) from error


def get_access_token(env: Mapping[str, str], session: requests.Session) -> str:
    log("Requesting access token from the OpenID provider...")
    url = "{}/protocol/openid-connect/token".format(env["OPENID_PROVIDER_URL"])
    try:
        response = session.post(
            url,
            data={
                "grant_type": "client_credentials",
                "scope": "openid",
                "client_id": env["OPENID_PROVIDER_CLIENT_ID"],
                "client_secret": env["OPENID_PROVIDER_CLIENT_SECRET"],
            },
            timeout=TIMEOUT,
        )
        token = response.json().get("access_token")
    except (requests.RequestException, ValueError, AttributeError) as error:
        raise BootstrapError("Failed to obtain access token: {}".format(error)) from error
    if not token:
        raise BootstrapError("Failed to obtain access token")
    return token


def wait_for(
    description: str,
    url: str,
    session: requests.Session,
    headers: Optional[Mapping[str, str]] = None,
    sleep: Callable[[float], None] = time.sleep,
) -> None:
    log("Waiting for {} to become reachable...".format(description))
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            status = session.get(url, headers=headers, timeout=TIMEOUT).status_code
        except requests.RequestException as error:
            status = type(error).__name__
        if status == 200:
            log("{} is reachable".format(description))
            return
        log("{} not reachable yet (HTTP {}, {}/{})...".format(description, status, attempt, MAX_RETRIES))
        sleep(RETRY_DELAY)
    raise BootstrapError("{} did not become reachable in time".format(description))


# --------------------------------------------------------------------------- schemas of another bundle version


def bundled_manifest(home: Path) -> Optional[Dict]:
    """The manifest (version, commit, registryUrl) of the schemas under ``home``, if there is one."""
    try:
        return json.loads((home / SCHEMAS_FOLDER / MANIFEST_NAME).read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


def registry_url(explicit: str, home: Path) -> str:
    """Registry base URL: the explicit one, else the one recorded in the bundled manifest, else the community one."""
    if explicit:
        return explicit.rstrip("/")
    manifest = bundled_manifest(home)
    if manifest and manifest.get("registryUrl"):
        return manifest["registryUrl"].rstrip("/")
    return DEFAULT_REGISTRY_URL


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def extract_schemas(bundle: Path, staging: Path) -> int:
    """Extract only ``<top>/shared-schemas/**`` of the bundle into ``staging``; return the number of files.

    Every entry is checked, but only the schemas are written, and only as regular files below ``staging``.
    """
    count = 0
    top = None
    with tarfile.open(bundle, mode="r:gz") as tar:
        for member in tar:
            path = PurePosixPath(member.name)
            if path.is_absolute() or ".." in path.parts or not path.parts:
                raise SchemaFetchError("Unsafe path in bundle: {!r}".format(member.name))
            if member.isdir():
                continue
            if len(path.parts) < 2:
                raise SchemaFetchError(
                    "Unexpected file outside the top-level folder of the bundle: {!r}".format(member.name)
                )
            if top is None:
                top = path.parts[0]
            elif path.parts[0] != top:
                raise SchemaFetchError("The bundle has more than one top-level folder: {!r}".format(member.name))
            if path.parts[1] != SCHEMAS_FOLDER:
                continue  # code and libraries are not part of the schemas
            if not member.isreg():
                raise SchemaFetchError("Unsupported entry in bundle: {!r}".format(member.name))
            target = staging.joinpath(*path.parts[1:])
            target.parent.mkdir(parents=True, exist_ok=True)
            with tar.extractfile(member) as source, open(target, "wb") as out:
                shutil.copyfileobj(source, out)
            count += 1
    if count == 0:
        raise SchemaFetchError("The bundle contains no {}/ folder".format(SCHEMAS_FOLDER))
    return count


def install_bundle(bundle: Path, home: Path, expected_sha256: str = "") -> None:
    """Replace ``home/shared-schemas`` with the schemas of a bundle file.

    With ``expected_sha256`` (the sha256 GitLab shows for the file) the bundle is checked first. Nothing under
    ``home`` changes unless the check and the extraction succeed.
    """
    pin = expected_sha256.strip().lower()
    if pin:
        actual = sha256_file(bundle)
        if actual != pin:
            raise SchemaFetchError(
                "Checksum mismatch of the downloaded bundle against SCHEMA_PACKAGE_SHA256: expected {}, got {}".format(
                    pin, actual
                )
            )
    home.mkdir(parents=True, exist_ok=True)
    staging = Path(tempfile.mkdtemp(prefix=".schemas-", dir=str(home)))
    try:
        try:
            count = extract_schemas(bundle, staging)
        except (tarfile.TarError, EOFError, OSError, zlib.error) as error:  # not a (complete) gzip tar
            raise SchemaFetchError("Cannot read the bundle: {}".format(error)) from error
        target = home / SCHEMAS_FOLDER
        if target.exists():
            shutil.rmtree(target)
        os.replace(staging / SCHEMAS_FOLDER, target)
    finally:
        shutil.rmtree(staging, ignore_errors=True)
    log("Installed {} schema files into {}".format(count, home / SCHEMAS_FOLDER))


def download(
    session: requests.Session, url: str, target: Path, headers: Mapping[str, str], attempts: int, delay: float
) -> None:
    last_error = "unknown error"
    for attempt in range(1, attempts + 1):
        try:
            with session.get(url, headers=headers, stream=True, timeout=(10, 120)) as response:
                if response.status_code == 200:
                    with open(target, "wb") as out:
                        for chunk in response.iter_content(chunk_size=1024 * 1024):
                            out.write(chunk)
                    return
                last_error = "HTTP {}".format(response.status_code)
                if response.status_code not in (408, 425, 429) and response.status_code < 500:
                    raise SchemaFetchError("Download of {} failed: {}".format(url, last_error))
        except requests.RequestException as error:
            last_error = str(error)
        if attempt < attempts:
            log("Download of {} failed ({}), retry {}/{}".format(url, last_error, attempt, attempts - 1))
            time.sleep(delay * attempt)
    raise SchemaFetchError("Download of {} failed after {} attempts: {}".format(url, attempts, last_error))


def fetch_schemas(
    version: str,
    home: Path,
    url: str = "",
    token: str = "",
    expected_sha256: str = "",
    session: Optional[requests.Session] = None,
    attempts: int = 5,
    delay: float = 3.0,
) -> None:
    """Download the bundle of ``version`` from the registry and install its schemas under ``home``."""
    base = registry_url(url, home)
    headers = {"PRIVATE-TOKEN": token} if token else {}
    session = session or requests.Session()
    file_url = "{}/{}/{}/{}".format(base, PACKAGE_NAME, quote(version, safe=""), BUNDLE_NAME)
    log("Fetching the schemas of {} version {} from {}".format(PACKAGE_NAME, version, base))
    with tempfile.TemporaryDirectory(prefix="schema-bundle-") as tmp:
        bundle = Path(tmp) / BUNDLE_NAME
        download(session, file_url, bundle, headers, attempts, delay)
        install_bundle(bundle, home, expected_sha256)


def prepare_schemas(env: Mapping[str, str], dest: Path, fetcher=fetch_schemas) -> None:
    """Log the active schemas, after replacing them with the requested bundle version if there is one."""
    version = env.get("SCHEMA_PACKAGE_VERSION", "").strip()
    if version:
        try:
            fetcher(
                version,
                dest,
                url=env.get("SCHEMA_PACKAGE_URL", ""),
                token=env.get("SCHEMA_PACKAGE_TOKEN", ""),
                expected_sha256=env.get("SCHEMA_PACKAGE_SHA256", ""),
            )
        except SchemaFetchError as error:
            if env.get("SCHEMA_PACKAGE_FALLBACK", "").lower() != "true":
                raise BootstrapError("Cannot use schema package {}: {}".format(version, error)) from error
            log("WARNING: cannot use schema package {} ({}), using the bundled schemas".format(version, error))
    manifest = bundled_manifest(dest)
    if manifest:
        log("Schema package in use: version {}, commit {}".format(
            manifest.get("version"), manifest.get("commit") or "unknown"))
    else:
        log("WARNING: no manifest found for the schemas under {}".format(dest / SCHEMAS_FOLDER))


# --------------------------------------------------------------------------- schema registration
#
# Based on deployments/scripts/DeploySharedSchemas.py, extended with the authority and the parallel registration.
# Nothing here reads the environment: everything is passed to deploy_shared_schemas().

SCHEMA_AUTHORITY_TO_REPLACE = "{{schema-authority}}"
LOAD_SEQUENCE_FOLDER = "osdu"
LOAD_SEQUENCE_FILE = "load_sequence.1.0.0.json"

# The schemas are registered under this authority unless another one is given. The Schema service accepts
# an authority without '.', ':' or white space (SCHEMA_KIND_REGEX), which in practice means letters, digits,
# '_' and '-'. The value is written into the schema JSON, so anything else is refused here.
DEFAULT_AUTHORITY = "osdu"
AUTHORITY_PATTERN = re.compile(r"^[A-Za-z0-9_-]+$")

SCHEMA_EXISTS = "Schema Id is already present"
PUBLISHED_SCHEMA_ERROR = (
    "Only schema in development stage can be updated",
    "Only schema in developement stage can be updated",
)
TRY_AGAIN = "Internal server error"

SUCCESS = "Success"
ALREADY_PUBLISHED = "AlreadyPublished"
RETRY = "PUT"
GIVE_UP = "Give up"

REQUEST_TIMEOUT = (10, 120)
MAX_WORKERS = 8
ABSTRACT_PREFIX = "Abstract"

_REF_PATTERN = re.compile(r'"\$ref"\s*:\s*"([^"#]+)')
_print_lock = threading.Lock()


class SchemaRegistrationError(Exception):
    """Raised when the registration cannot continue (bad input or early exit)."""


def _emit(lines: List[str]) -> None:
    """Print the lines of one schema together, so parallel output does not interleave."""
    if lines:
        with _print_lock:
            print("\n".join(lines), flush=True)


def load_json(path: str):
    """Load a JSON file."""
    try:
        with open(path, "r", encoding="utf-8") as text_file:
            return json.load(text_file)
    except FileNotFoundError as error:
        raise SchemaRegistrationError("Given file path not found::{}".format(error)) from error


def _identity(schema_as_str: str) -> Tuple[str, str]:
    """Return ``(kind, entityType)`` of a schema payload."""
    try:
        schema = json.loads(schema_as_str)
        identity = schema.get("schemaInfo", {}).get("schemaIdentity", {})
        entity_type = identity.get("entityType", "")
        kind = "{}:{}:{}:{}.{}.{}".format(
            identity.get("authority", ""),
            identity.get("source", ""),
            entity_type,
            identity.get("schemaVersionMajor", 0),
            identity.get("schemaVersionMinor", 0),
            identity.get("schemaVersionPatch", 0),
        )
        return kind, entity_type
    except Exception as error:
        raise SchemaRegistrationError("Invalid JSON in payload {}".format(error)) from error


def validate_authority(authority: Optional[str]) -> str:
    """Return the authority to register the schemas under (``osdu`` when empty) or refuse an invalid one."""
    authority = (authority or "").strip() or DEFAULT_AUTHORITY
    if not AUTHORITY_PATTERN.match(authority):
        raise SchemaRegistrationError(
            "Invalid schema authority {!r}: use only letters, digits, '_' and '-' "
            "(no '.', ':' or white space)".format(authority)
        )
    return authority


# --------------------------------------------------------------------------- planning


class Entry(NamedTuple):
    """One schema of the load sequence."""

    kind: str
    entity_type: str
    key: str  # authority independent id: source:entityType:version
    refs: frozenset  # keys of the schemas this one refers to
    schema: str  # payload with the authority already replaced


def _key_of(kind: str) -> str:
    return kind.split(":", 1)[1] if kind.count(":") == 3 else kind


def make_entry(schema: str) -> Entry:
    kind, entity_type = _identity(schema)
    refs = set()
    for ref in _REF_PATTERN.findall(schema):
        ref = ref[:-5] if ref.endswith(".json") else ref
        if ref.count(":") == 3:
            refs.add(_key_of(ref))
    refs.discard(_key_of(kind))
    return Entry(kind, entity_type, _key_of(kind), frozenset(refs), schema)


def _is_abstract(entry: Entry) -> bool:
    return entry.entity_type.startswith(ABSTRACT_PREFIX)


def plan_parallel(entries: List[Entry]) -> Optional[Tuple[List[int], List[List[int]]]]:
    """Split ``entries`` (in load-sequence order) into the sequential abstract phase and
    parallel chains.

    Returns ``(abstract_indexes, chains)`` where each chain is a list of indexes in
    load-sequence order, or ``None`` when the load sequence cannot be parallelised
    safely (duplicate kinds, an abstract schema referring to a non-abstract one, or a
    schema listed before something it refers to).
    """
    position: Dict[str, int] = {}
    for index, entry in enumerate(entries):
        if entry.key in position:
            return None
        position[entry.key] = index

    parent: Dict[str, str] = {}

    def find(entity: str) -> str:
        parent.setdefault(entity, entity)
        while parent[entity] != entity:
            parent[entity] = parent[parent[entity]]
            entity = parent[entity]
        return entity

    abstract = [index for index, entry in enumerate(entries) if _is_abstract(entry)]
    for index, entry in enumerate(entries):
        find(entry.entity_type)
        for ref in entry.refs:
            target = position.get(ref)
            if target is None:
                continue  # not part of this load sequence, nothing to order against
            other = entries[target]
            if _is_abstract(entry):
                if not _is_abstract(other) or target > index:
                    return None
            elif not _is_abstract(other):
                if target > index:
                    return None
                parent[find(entry.entity_type)] = find(other.entity_type)

    chains: Dict[str, List[int]] = {}
    for index, entry in enumerate(entries):
        if not _is_abstract(entry):
            chains.setdefault(find(entry.entity_type), []).append(index)
    return abstract, list(chains.values())


# --------------------------------------------------------------------------- registration


class _Sessions:
    """A shared session if one was given, otherwise one session per thread."""

    def __init__(self, session: Optional[requests.Session]):
        self._shared = session
        self._local = threading.local()

    def get(self) -> requests.Session:
        if self._shared is not None:
            return self._shared
        if not hasattr(self._local, "session"):
            self._local.session = requests.Session()
        return self._local.session


class _Registrar:
    def __init__(self, url: str, headers: dict, early_exit: bool, sessions: _Sessions):
        self.url = url
        self.headers = headers
        self.early_exit = early_exit
        self.sessions = sessions
        self.messages: List[str] = []
        self.stop = threading.Event()

    def register_one(self, kind: str, schema: str) -> None:
        out: List[str] = []
        try:
            method = "PUT"
            try_it = "Try {} for id: {}"
            out.append(try_it.format(method, kind))
            is_error, message, outcome = self._send(schema, out)
            if outcome == RETRY:
                out.append(try_it.format(method, kind))
                is_error, message, outcome = self._send(schema, out)

            if is_error:
                message = "Error with kind {}: Message: {}".format(kind, message)
                out.append(message)
                self.messages.append(message)
            elif outcome == SUCCESS:
                out.append("The kind {} was registered successfully.".format(kind))
            elif outcome == ALREADY_PUBLISHED:
                out.append(
                    "The kind {} was already registered with status PUBLISHED "
                    "and was not updated.".format(kind)
                )
            elif outcome == RETRY:
                out.append("Warning: kind {} was not confirmed after a retry ({}).".format(kind, TRY_AGAIN))
        finally:
            _emit(out)

    def _send(self, schema: str, out: List[str]) -> Tuple[bool, str, str]:
        try:
            response = self.sessions.get().request(
                "PUT",
                self.url,
                headers=self.headers,
                data=schema.encode("utf-8"),
                timeout=REQUEST_TIMEOUT,
            )
        except requests.RequestException as error:
            if self.early_exit:
                raise SchemaRegistrationError("Request failed: {}".format(error)) from error
            return True, str(error), GIVE_UP
        return self._evaluate(response, out)

    def _evaluate(self, response: requests.Response, out: List[str]) -> Tuple[bool, str, str]:
        code = response.status_code
        message = ""
        outcome = GIVE_UP
        error = code not in range(200, 300)
        if not error:
            return False, message, SUCCESS

        out.extend([response.text, str(response.url), str(code)])
        try:
            payload = json.loads(response.text)
            message = payload.get("error", {}).get("message", "")
            if message == SCHEMA_EXISTS:
                outcome = RETRY  # it might be in DEVELOPMENT, then PUT can overwrite it
            elif message in PUBLISHED_SCHEMA_ERROR:
                outcome = ALREADY_PUBLISHED  # nothing to bootstrap
                error = False
            elif message == TRY_AGAIN:
                outcome = RETRY
                error = False
                if self.early_exit:
                    response.raise_for_status()
            # everything else is an error
        except Exception as caught:
            message = str(caught)
            if self.early_exit:
                raise SchemaRegistrationError(message) from caught
        return error, message, outcome

    def register_all(self, entries: List[Entry], indexes) -> None:
        """Register ``entries[i]`` for the given indexes, one after the other."""
        for index in indexes:
            if self.stop.is_set():
                return
            entry = entries[index]
            try:
                self.register_one(entry.kind, entry.schema)
            except SchemaRegistrationError:
                self.stop.set()
                raise


def _register(registrar: _Registrar, entries: List[Entry], workers: int) -> None:
    sequential = range(len(entries))
    if workers <= 1 or len(entries) < 2:
        registrar.register_all(entries, sequential)
        return

    plan = plan_parallel(entries)
    if plan is None:
        _emit(["Warning: the load sequence cannot be parallelised safely, registering sequentially."])
        registrar.register_all(entries, sequential)
        return

    abstract, chains = plan
    _emit(
        [
            "Registering {} abstract schemas, then {} schemas in {} chains with {} workers.".format(
                len(abstract), len(entries) - len(abstract), len(chains), workers
            )
        ]
    )
    registrar.register_all(entries, abstract)
    with ThreadPoolExecutor(max_workers=workers, thread_name_prefix="schema") as pool:
        futures = [pool.submit(registrar.register_all, entries, chain) for chain in chains]
        first_error: Optional[BaseException] = None
        for future in futures:
            try:
                future.result()
            except BaseException as error:  # keep the first failure, let the others stop
                registrar.stop.set()
                first_error = first_error or error
    if first_error is not None:
        raise first_error


def deploy_shared_schemas(
    schema_url: str,
    bearer_token: str,
    authority: str = DEFAULT_AUTHORITY,
    schemas_root: str = DEFAULT_HOME,
    early_exit: bool = False,
    session: Optional[requests.Session] = None,
    workers: int = 1,
) -> List[str]:
    """Register all schemas listed in the load sequence under ``authority``.

    ``schemas_root`` is the folder that contains ``shared-schemas/``; the
    ``relativePath`` entries of the load sequence are relative to it. The
    ``{{schema-authority}}`` placeholder of every schema is replaced by ``authority``.
    ``workers`` > 1 registers independent schemas in parallel (see the description at the
    top of this file); a supplied ``session`` is then shared by all threads, otherwise
    every thread uses its own.
    Returns the list of failure messages; an empty list means success.
    """
    if not schema_url:
        raise SchemaRegistrationError("The schema service URL is not specified")
    if not bearer_token:
        raise SchemaRegistrationError("The bearer token is not specified")
    if not 1 <= workers <= MAX_WORKERS:
        raise SchemaRegistrationError("workers must be between 1 and {}".format(MAX_WORKERS))

    authority = validate_authority(authority)
    headers = {"Content-Type": "application/json", "Authorization": bearer_token}
    registrar = _Registrar(schema_url, headers, early_exit, _Sessions(session))

    start = time.time()
    sequence = load_json(os.path.join(schemas_root, SCHEMAS_FOLDER, LOAD_SEQUENCE_FOLDER, LOAD_SEQUENCE_FILE))
    entries: List[Entry] = []
    for item in sequence:
        schema_file = os.path.join(schemas_root, item["relativePath"])
        try:
            with open(schema_file, "r", encoding="utf-8") as handle:
                schema = handle.read()
        except FileNotFoundError as error:
            raise SchemaRegistrationError("Schema file not found::{}".format(error)) from error
        entries.append(make_entry(schema.replace(SCHEMA_AUTHORITY_TO_REPLACE, authority)))

    _register(registrar, entries, workers)
    registered = len(entries)

    messages = registrar.messages
    elapsed = time.time() - start
    print("This update took {:.2f} seconds.".format(elapsed), flush=True)
    if messages:
        print("Following schemas failed:", flush=True)
        print("\n".join(messages), flush=True)
    else:
        print(
            "All {} schemas registered, updated or "
            "left unchanged because of status PUBLISHED.".format(registered),
            flush=True,
        )
    return messages


# --------------------------------------------------------------------------- run


def run(
    env: Mapping[str, str],
    session: Optional[requests.Session] = None,
    sleep: Callable[[float], None] = time.sleep,
    dest: Path = Path(DEFAULT_HOME),
    ready_file: Optional[str] = None,
    fetcher=fetch_schemas,
) -> None:
    """Run all bootstrap steps except staying alive."""
    validate_env(env)
    workers = parse_workers(env)
    authority = parse_authority(env)
    session = session or requests.Session()
    log("Starting schema bootstrap, registering the schemas under authority '{}'...".format(authority))

    prepare_schemas(env, dest, fetcher)

    token = get_access_token(env, session)
    wait_for(
        "Entitlements service",
        env["ENTITLEMENTS_HOST"] + ENTITLEMENTS_PATH,
        session,
        {
            "Content-Type": "application/json",
            "data-partition-id": env["DATA_PARTITION"],
            "Authorization": "Bearer {}".format(token),
        },
        sleep,
    )
    log("Entitlements provisioning completed successfully")
    sleep(ENTITLEMENTS_SETTLE_SECONDS)
    wait_for("Schema service", env["SCHEMA_URL"] + INFO_PATH, session, None, sleep)

    try:
        failed = deploy_shared_schemas(
            env["SCHEMA_URL"] + SCHEMAS_PATH,
            "Bearer {}".format(token),
            authority=authority,
            schemas_root=str(dest),
            early_exit=True,
            # one session per thread when registering in parallel
            session=session if workers == 1 else None,
            workers=workers,
        )
    except SchemaRegistrationError as error:
        raise BootstrapError("Schema registration aborted: {}".format(error)) from error
    if failed:
        raise BootstrapError("{} schema(s) failed to register".format(len(failed)))

    Path(ready_file or env.get("BOOTSTRAP_READY_FILE", DEFAULT_READY_FILE)).touch()
    log("Bootstrap finished successfully")


def wait_until_stopped() -> None:
    """Keep the container (and its readiness probe) alive; exit promptly on SIGTERM."""
    stop = threading.Event()
    for signum in (signal.SIGTERM, signal.SIGINT):
        signal.signal(signum, lambda *_: stop.set())
    stop.wait()
    log("Received stop signal, exiting")


def parse_args(argv: List[str], env: Mapping[str, str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Bootstrap the Schema service with the shared OSDU schemas. "
                    "Settings come from environment variables.")
    parser.add_argument(
        "--home",
        default=env.get("BOOTSTRAP_HOME") or DEFAULT_HOME,
        help="folder that contains shared-schemas/ (env BOOTSTRAP_HOME, default {})".format(DEFAULT_HOME))
    parser.add_argument(
        "--exit-when-done",
        action="store_true",
        default=env.get("BOOTSTRAP_EXIT_WHEN_DONE", "").lower() == "true",
        help="exit after the schemas are registered instead of staying alive for the readiness probe "
             "(env BOOTSTRAP_EXIT_WHEN_DONE=true)")
    return parser.parse_args(argv)


def main(argv: Optional[List[str]] = None) -> int:
    args = parse_args(sys.argv[1:] if argv is None else argv, os.environ)
    try:
        run(os.environ, dest=Path(args.home))
    except BootstrapError as error:
        log(str(error), error=True)
        return 1
    if args.exit_when_done:
        return 0
    wait_until_stopped()
    return 0


if __name__ == "__main__":
    sys.exit(main())
