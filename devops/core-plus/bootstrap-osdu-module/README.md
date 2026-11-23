# Schema service bootstrap (core-plus)

Registers the shared OSDU schemas (`deployments/shared-schemas`) with the Schema service on the system endpoint
`/api/schema-service/v1/schemas/system`. It is a self-contained Python module: the image is built from one
**bootstrap bundle** (code, pinned libraries and schemas) and nothing else is copied from `deployments/`.

| File | Purpose |
| --- | --- |
| `bootstrap_schema.py` | The whole bootstrap in one file: configuration, optional download of another schema version, token, waits, schema registration (port of `deployments/scripts/DeploySharedSchemas.py`), readiness file |
| `requirements.txt` / `requirements-dev.txt` | Runtime libraries (pinned with hashes, they go into the bundle) / test dependencies |
| `tests/` | Unit tests |

The bundle is built and published by one CI job, `schema-package`, in
[`../pipeline/override-stages.yml`](../pipeline/override-stages.yml), which runs
[`deployments/scripts/schema-package.py`](../../../deployments/scripts/schema-package.py).

## Configuration

Settings are environment variables. In the chart the first group comes from `values.yaml` (`data.*`, `global.*`) and
the OpenID settings from the secret `conf.bootstrapSecretName` (default `datafier-secret`).

| Variable | Helm value | Default | Meaning |
| --- | --- | --- | --- |
| `DATA_PARTITION`, `SCHEMA_URL`, `ENTITLEMENTS_HOST` | `global.dataPartitionId`, `data.schemaHost`, `data.entitlementsHost` | required | Partition and service addresses |
| `OPENID_PROVIDER_URL`, `OPENID_PROVIDER_CLIENT_ID`, `OPENID_PROVIDER_CLIENT_SECRET` | secret | required | Client-credentials token |
| `SCHEMA_AUTHORITY` | `data.schemaAuthority` | `osdu` | Authority the schemas are registered under, see [below](#registering-under-another-authority) |
| `BOOTSTRAP_WORKERS` | `data.bootstrapWorkers` | `1` (chart: `4`) | Registration threads, 1 to 8 |
| `SCHEMA_PACKAGE_VERSION`, `SCHEMA_PACKAGE_URL`, `SCHEMA_PACKAGE_SHA256`, `SCHEMA_PACKAGE_FALLBACK` | `data.schemaPackage*` | empty | Use the schemas of another published version, see [Overriding the schemas](#overriding-the-schemas) |
| `SCHEMA_PACKAGE_TOKEN` | secret only | empty | `PRIVATE-TOKEN` for a private package registry |
| `BOOTSTRAP_HOME` / `--home` | - | `/opt` | Folder that contains `shared-schemas/` |
| `BOOTSTRAP_EXIT_WHEN_DONE` / `--exit-when-done` | - | `false` | Exit after registering instead of staying alive |
| `BOOTSTRAP_READY_FILE` | - | `/tmp/bootstrap_ready` | Readiness file |

## How it runs

1. Validates the required settings.
2. If `SCHEMA_PACKAGE_VERSION` is set, replaces the bundled schemas by those of that version.
3. Logs the version and commit of the schemas in use (from their manifest).
4. Requests a token, then waits for Entitlements (`/api/entitlements/v2/groups`) and the Schema service
   (`/api/schema-service/v1/info`), up to 60 tries 3 seconds apart.
5. Registers the schemas of `shared-schemas/osdu/load_sequence.1.0.0.json`, stopping at the first unexpected error.
   Already `PUBLISHED` schemas are left as they are. With `BOOTSTRAP_WORKERS` above 1 the `Abstract*` schemas go first,
   one at a time, then independent schemas in parallel (versions of one entity stay in order). If the load sequence
   is not safe to parallelise, it falls back to one at a time.
6. Creates the readiness file and waits for SIGTERM, or exits with `--exit-when-done`.

### Registering under another authority

The schemas carry the placeholder `{{schema-authority}}` as the first part of their kind. The bootstrap replaces it by
`SCHEMA_AUTHORITY` in the schema identity, the id and every `$ref`, so one bundle serves any authority:
`SCHEMA_AUTHORITY=acme` registers `acme:wks:...` instead of `osdu:wks:...`.

* Only letters, digits, `_` and `-` are allowed. An invalid value stops the bootstrap before anything is sent, and
  `helm install` fails with a message.
* The schemas go to `/schemas/system`, so every data partition sees them.
* Anything that hard-codes `osdu:wks:...` kinds (manifests, ingestion, test data) will not find the new ones.
* Nothing is replaced. The shipped schemas are mostly `PUBLISHED`, which cannot be changed or deleted through the API,
  so a wrong authority has to be removed from the database and the bucket. Try it on a development environment first.
* For several authorities, run the bootstrap once for each.

## Bootstrap bundle

`schema-bootstrap-bundle.tar.gz` is the only artifact. The `schema-package` job (image `python:3.10-alpine3.24`)
builds it once per pipeline (copy the code, `pip install` the pinned libraries, copy the `*.json` schemas, write the
manifest, `tar`) with `deployments/scripts/schema-package.py` and, for a release, uploads it to the GitLab Generic
Package Registry. The image is built from the same
file.

```text
schema-bootstrap-<version>/
  bootstrap_schema.py
  libs/            requests and its dependencies (pure-Python wheels)
  shared-schemas/  the schemas (only *.json), VERSION and manifest.json
```

Registry package `schema-bootstrap`, per version: the bundle. `manifest.json` in `shared-schemas/` records the
`version`, the `commit` and the `registryUrl` of the pipeline that built it.

* **Checksum.** GitLab computes the sha256 of every file it stores and shows it in the package's **Assets** list
  (Deploy > Package registry), so the bundle carries no checksum file and the schemas are not hashed one by one.
  To pin a version, copy that value into `SCHEMA_PACKAGE_SHA256`, see [Overriding the schemas](#overriding-the-schemas).
* **Pinned libraries.** `requirements.txt` pins every library to an exact version and the sha256 of its pure-Python
  wheel, so builds are identical and `pip` rejects any other file. The bundle runs on any OS with Python 3.10 or newer.
  To update a library, change the version and take the sha256 of its `py3-none-any` wheel from
  `https://pypi.org/pypi/<name>/<version>/json`.
* **Versions.** The version is the content of `deployments/shared-schemas/VERSION`, `MAJOR.MINOR.PATCH` (for example
  `27.1.0`). A push to the default branch that changes `VERSION` or a schema uploads the same bundle as
  `27.1.0`, `27.1` and `27`; nothing else is published (other branches, merge requests, tags and other pushes only build
  the bundle). `27.1.0` always means one set of schemas: the job fails if that version already exists with different schemas, so `VERSION` has to be raised.
  The whole process (what is published when, the job messages, the guard that keeps `27` and `27.1` from moving backwards, pinning,
  trying unreleased schemas and the GitLab setting) is in
  [`deployments/shared-schemas/RELEASE.md`](../../../deployments/shared-schemas/RELEASE.md).

### Build and run locally

```bash
docker build -f devops/core-plus/bootstrap-osdu-module/Dockerfile -t schema-bootstrap .   # from the repository root
```

The `Dockerfile` takes the bundle from one of two stages, chosen by the build argument `BUNDLE`:

* `local` (default): the stage `bundle-local` builds it from `deployments/shared-schemas` with the same steps as the
  `schema-package` job, with the version `local`. This needs access to PyPI.
* `prebuilt`: the stage `bundle-prebuilt` copies the file the CI build job put in `dist/schema-package`, so the image
  holds exactly the published bundle. CI sets `--build-arg BUNDLE=prebuilt` through `CIMPL_BUILD_ARGS`.

### Standalone: download and run

The bundle needs only Python 3.10 or newer on the machine.

```bash
curl -fSL "<registry>/schema-bootstrap/<version>/schema-bootstrap-bundle.tar.gz" | tar xz
cd schema-bootstrap-<version>
export DATA_PARTITION=... SCHEMA_URL=... ENTITLEMENTS_HOST=...
export OPENID_PROVIDER_URL=... OPENID_PROVIDER_CLIENT_ID=... OPENID_PROVIDER_CLIENT_SECRET=...
python3 bootstrap_schema.py --home . --exit-when-done
```

`<registry>` is `https://<gitlab>/api/v4/projects/<project id>/packages/generic`. For a private project add
`-H "PRIVATE-TOKEN: <token>"` to `curl`.

## Overriding the schemas

By default the pod uses the schemas of its image. Set `data.schemaPackageVersion` (or `SCHEMA_PACKAGE_VERSION`) to use
the schemas of another published version without rebuilding the image:

| Helm value | Variable | Meaning |
| --- | --- | --- |
| `data.schemaPackageVersion` | `SCHEMA_PACKAGE_VERSION` | Version to download: `27.1.0`, `27.1` (newest patch of 27.1) or `27` (newest 27.x.x). Write it in quotes in the values file, the chart rejects a number. Empty means the bundled schemas. |
| `data.schemaPackageUrl` | `SCHEMA_PACKAGE_URL` | Registry base URL. Defaults to the URL recorded in the bundled manifest, then to the community project. |
| `data.schemaPackageSha256` | `SCHEMA_PACKAGE_SHA256` | Optional sha256 of the bundle **file**, as GitLab shows it in the package's Assets (or `sha256sum` prints it). Checked before anything is extracted. |
| `data.schemaPackageFallback` | `SCHEMA_PACKAGE_FALLBACK` | `true` uses the bundled schemas if the download fails; otherwise the job fails. |

The pod downloads `<registry>/schema-bootstrap/<version>/schema-bootstrap-bundle.tar.gz`, compares its sha256 with
`SCHEMA_PACKAGE_SHA256` when that is set, and extracts only `shared-schemas/`. Nothing is replaced unless the pin and the
extraction succeed. Without a pin only HTTPS protects the download (a truncated file still fails to extract), so pin
the versions you rely on. The pod needs network access to the registry.

### Trying another version in a running environment

To test a published bundle without a Helm release, run the bootstrap with `SCHEMA_PACKAGE_VERSION` set. Both ways need
an image built from this module (it contains `/opt/bootstrap_schema.py`; an older shell-based image ignores the
setting) and a published version. Use an exact released version such as `27.1.0`, not the aliases `27` and `27.1`.
The examples use the deployment and container `schema-bootstrap`; take the real names from
`kubectl -n $NS get deploy schema-bootstrap -o jsonpath='{.spec.template.spec.containers[*].name}'`.

**Without restarting the container.** Run the bootstrap once more inside the running pod (it needs at least one
replica). The new process inherits the container's environment, so only the variables you list change:

```bash
kubectl -n $NS exec deploy/schema-bootstrap -c schema-bootstrap -- \
  env SCHEMA_PACKAGE_VERSION=27.1.0 \
  python3 /opt/bootstrap_schema.py --exit-when-done
```

The output is printed in your terminal and the exit code is `0` on success. The downloaded schemas replace
`/opt/shared-schemas` in this container only, until the pod restarts. If a pin for another version is configured, add
`SCHEMA_PACKAGE_SHA256=` (empty) or this version's hash. To try the next version, run the same command with another
version.

**With a restart.** Set the variable on the deployment. This changes the pod template, so Kubernetes starts a new pod,
and the value overrides the same key from the ConfigMap:

```bash
kubectl -n $NS set env deploy/schema-bootstrap -c schema-bootstrap SCHEMA_PACKAGE_VERSION=27.1.0
kubectl -n $NS rollout status deploy/schema-bootstrap
kubectl -n $NS logs deploy/schema-bootstrap -c schema-bootstrap
```

Remove it again with `kubectl -n $NS set env deploy/schema-bootstrap -c schema-bootstrap SCHEMA_PACKAGE_VERSION-`.
If Helm or Flux manages the deployment, the next reconcile reverts a manual change. Set `data.schemaPackageVersion`
in the values for a lasting setting.

**What to check.** The log shows the download and the package in use, and the manifest shows it afterwards:

```text
Fetching the schemas of schema-bootstrap version 27.1.0 from https://...
Installed <N> schema files into /opt/shared-schemas
Schema package in use: version 27.1.0, commit <sha>
```

```bash
kubectl -n $NS exec deploy/schema-bootstrap -c schema-bootstrap -- cat /opt/shared-schemas/manifest.json
```

The Schema service keeps schemas that are already `PUBLISHED`, so in an environment that already has them only new
schemas and `DEVELOPMENT` ones change.

### Loading schemas from a private GitLab project

A private project needs an image pull secret for the image. Downloading schemas (`data.schemaPackageVersion` set) needs a
token as well: an image pull secret does not cover the package registry.

1. **Registry URL.** Use the API address with `/` in the project path written as `%2F`:
   `https://<gitlab-host>/api/v4/projects/group%2Fsubgroup%2Fschema-service/packages/generic`. Leave
   `data.schemaPackageUrl` empty when the same project's pipeline built the image, because the image records this URL.
2. **Token.** Create a project or group access token that can read the package registry (it is sent as `PRIVATE-TOKEN`;
   deploy tokens are not supported) and add it to the secret the bootstrap pod loads, then restart the pod:

   ```bash
   kubectl -n <ns> patch secret datafier-secret --type merge \
     -p '{"stringData":{"SCHEMA_PACKAGE_TOKEN":"<token>"}}'
   ```

   Every workload that loads this secret receives the token, so use a dedicated secret (with the `OPENID_PROVIDER_*`
   keys plus the token) if it is shared. Never put the token in the ConfigMap or `values.yaml`.
3. **Version.** Set `data.schemaPackageVersion` to a version that exists in that project, and `data.schemaPackageUrl` if
   needed.
4. **Check** from a pod in the cluster; expect `200`:

   ```bash
   curl -s -o /dev/null -w "%{http_code}\n" -H "PRIVATE-TOKEN: <token>" "<registry url>/schema-bootstrap/<version>/schema-bootstrap-bundle.tar.gz"
   ```

   GitLab can answer `401`, `403` or `404` for missing access, so a `404` may also mean a wrong version or URL.

## Tests

```bash
pip install -r devops/core-plus/bootstrap-osdu-module/requirements-dev.txt
cd devops/core-plus/bootstrap-osdu-module && python -m pytest tests
```

## Example output

```
Starting schema bootstrap, registering the schemas under authority 'osdu'...
Schema package in use: version 27.1.0, commit 1a2b3c4d...
Registering 144 abstract schemas, then 1303 schemas in 821 chains with 4 workers.
...
The kind osdu:wks:work-product-component--WellboreTrajectory:1.0.0 was registered successfully.
...
This update took 156.52 seconds.
All 1447 schemas registered, updated or left unchanged because of status PUBLISHED.
Bootstrap finished successfully
```
