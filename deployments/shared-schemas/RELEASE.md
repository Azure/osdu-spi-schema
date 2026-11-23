# Releasing the shared schemas

A release of the schemas is a **bundle**, `schema-bootstrap-bundle.tar.gz`, published to the GitLab generic package
registry of this project as the package `schema-bootstrap`. It holds the schemas of this folder, the bootstrap code
and its pinned libraries (see the [bootstrap module README](../../devops/core-plus/bootstrap-osdu-module/README.md)).

The release of the schemas is independent of the releases of the Schema service. Git tags, `release/*` branches and
`pom.xml` play no part in it: **the version is the content of the [`VERSION`](VERSION) file and nothing else.**

## The `VERSION` file

One line, `MAJOR.MINOR.PATCH`, for example `27.1.0`.

| Content | Result |
| --- | --- |
| `27.1.0`, `28.0.0`, `0.3.12` | accepted |
| `28.0`, `28`, `M27.1`, `27.1.01`, empty file | the job fails with `VERSION must be MAJOR.MINOR.PATCH (for example 28.0.0)` |

## What a release publishes

The job uploads the same bundle under three names: the exact version `X.Y.Z`, the minor alias `X.Y` and the major
alias `X`. The aliases always point to the newest patch of that minor or major.

| Release push (see below) | Uploaded |
| --- | --- |
| `VERSION` is `27.1.0`, first time | `27.1.0`, `27.1` and `27` (all new) |
| Schemas changed, `VERSION` still `27.1.0` after `27.1.0` was published | **the job fails** before uploading anything: `27.1.0` always means one set of schemas, bump `VERSION` |
| Same schemas as published, `VERSION` still `27.1.0` (a re-run, a triggered pipeline) | `27.1.0` is not uploaded again. `27.1` and `27` are uploaded as before |
| `VERSION` becomes `27.1.1` | `27.1.1` is new. `27.1` and `27` are overwritten. `27.1.0` stays as it was |
| `VERSION` becomes `27.2.0` | `27.2.0` and `27.2` are new. `27` is overwritten. `27.1` stays |
| `VERSION` becomes `28.0.0` | `28.0.0`, `28.0` and `28` are new. `27` stays |
| `VERSION` becomes `27.1.2` while `27.2.0` exists (hotfix) | `27.1.2` is new, `27.1` is overwritten, `27` is left as it is |

## How to release

1. Change the schemas in this folder.
2. Set `VERSION` in the same merge request. A change to the schemas needs a new version, because a published version
   never changes:
   * a new milestone sets `X.Y.0` (milestone M27.1 is `27.1.0`, M28.0 is `28.0.0`);
   * a fix to a milestone that is already published raises the patch (`27.1.0` to `27.1.1`).
3. Merge to the default branch. The `schema-package` job of the branch pipeline (a push pipeline or the pipeline it triggers)
   publishes the release.
4. Check the job log (see below) and the package list in the GitLab project under *Deploy > Package registry*.

If the version was not raised, the job fails with `ERROR: 27.1.0 is already published with different schemas`, and
nothing is uploaded. The same check runs in the pipelines of the merge request (see *The check before the merge*
below), so the failure shows before the merge. The release job runs on the push to the protected branch, after the merge, so it cannot stop
a merge that was made anyway: the merged schemas then stay unpublished until a follow-up merge request raises
`VERSION`.

## Which pipelines publish

| Pipeline | Uploaded |
| --- | --- |
| Push to the default branch that changes `VERSION` or a `*.json` schema | `X.Y.Z`, `X.Y` and `X` |
| Anything else: any other branch (protected or trusted ones too), a merge request, a tag, a push without such a change | nothing. The bundle is only built for the bootstrap image and kept as a job artifact. The job still runs the check below |

### The check before the merge

Every pipeline that is not a release runs the `schema-package` job in `check` mode: it builds the bundle and
compares its schemas with the published `X.Y.Z` of the `VERSION` in that branch, exactly as the release does, but
uploads nothing. This includes the pipelines of a merge request into the default branch. It does not depend on the
pipeline source (a merge request may run as a branch pipeline, for example on a trusted branch) or on which files
changed:

| `VERSION` in the branch | Result |
| --- | --- |
| not published yet | passes (`OK 27.1.1: not published yet, nothing to compare (schemas sha256 <12 characters>)`) |
| published with the same schemas (this includes a branch that did not touch the schemas) | passes (`OK 27.1.0: already published with the same schemas (sha256 <12 characters>), nothing to do`) |
| published with different schemas | **fails** (`ERROR: 27.1.0 is already published with different schemas ...`): raise `VERSION` in the merge request |

So a schema change that forgot to raise `VERSION` is stopped in the merge request and not found after the merge. The
same job also builds the bootstrap image, so a failed check stops the image build of that pipeline too. The check
reads the package registry, so every such pipeline needs access to it. The release job runs the same comparison
after the push to the default branch, as a second line of defence.

The version recorded in the bundle (`manifest.json`) is the exact `X.Y.Z` of the `VERSION` file, together with the
commit the bundle was built from.

**Which branches may release.** Only the default branch (`CIMPL_STACK=1` is defined only for protected
branches, and the rule also requires a branch pipeline started by a push or triggered by another pipeline, so merge
requests and tags never release). Every other branch, including `release/*` and the trusted ones, builds the bundle
and the image and publishes nothing. GitLab cannot compare changes in a triggered pipeline, so such a pipeline on
the default branch always runs the publish step; that is harmless for a version that is already published with the
same schemas. The bundle always holds the schemas of the default branch. A patch such as `27.1.4` published after
`27.2.0` therefore carries the current schemas, not those of milestone M27.1: the guard keeps the aliases from moving
to an older version (`27.1` moves to `27.1.4`, `27` stays at `27.2.0`), but it cannot restore old content. The schemas
of an older milestone can only be patched from a branch that has them, and such a branch cannot release.

## What the job prints

Every name is announced before it is uploaded:

| Line | Meaning |
| --- | --- |
| `NEW 27.1.1 with 27.1.1` | the package did not exist |
| `OVERWRITE 27.1 with 27.1.1` | an alias moves forward (or is uploaded again for the same version) |
| `SAME 27.1.0: already published with the same schemas, not uploaded again` | the exact version exists with the same schemas, so it is left as it is |
| `SKIP 27: 27.2.0 is newer than 27.1.2, not changed` | the guard, see below |
| `ERROR: 27.1.0 is already published with different schemas (published ..., this build ...)` | the exact version exists with other schemas. The job fails before anything is uploaded; bump `VERSION` and run the pipeline again |
| `OK 27.1.1: not published yet, nothing to compare ...` or `OK 27.1.0: already published with the same schemas (sha256 ...), nothing to do` | the check in a pipeline that is not a release passed; nothing is uploaded |
| a Python traceback with `HTTPError` | the registry could not be read or written; the job fails, re-run it |

### The guard

The aliases `X` and `X.Y` only move forward. Before any upload the job lists the published versions of the package
`schema-bootstrap` (one call to the GitLab Packages API). If a **newer** `X.Y.Z` of the alias's series is published,
for example on a hotfix of an older minor or a re-run of an old pipeline, the alias is left as it is and the job logs
`SKIP`. The exact `X.Y.Z` is never overwritten. If it is already in that list the job reads the published bundle
(with its `JOB-TOKEN`) and compares the schemas:

* **Same schemas:** it is not uploaded again (`SAME`).
* **Different schemas:** the job fails before any upload (`ERROR`), so the aliases stay as they are too. The
  comparison covers the schema files and `VERSION`, not the code, the libraries or the commit recorded in
  `manifest.json`, so a rebuild of the same schemas is never mistaken for a change.

## Using a version

* **Helm:** set `data.schemaPackageVersion` to `27.1.0`, `27.1` or `27`, in quotes in the values file. YAML turns an
  unquoted `27.10` into `27.1` and `28.0` into `28`, so the chart rejects a number. With `--set` use `--set-string`.
* **Pin only the exact version.** The names of one release carry the same file, but `X` and `X.Y` change with every
  patch, so a sha256 pin on them (`data.schemaPackageSha256`) breaks. `X.Y.Z` always means one set of schemas, because
  the job refuses to publish different schemas under a version that exists, so two deployments pinned to the same
  `X.Y.Z` load the same schemas.
* **Which patch is behind a name:** the manifest of the bundle records it. For example for `27.1`:

  ```bash
  curl -fsSL "https://community.opengroup.org/api/v4/projects/osdu%2Fplatform%2Fsystem%2Fschema-service/packages/generic/schema-bootstrap/27.1/schema-bootstrap-bundle.tar.gz" \
    | tar -xzO --wildcards '*/shared-schemas/manifest.json'
  ```

  The bootstrap logs the same data at start-up (`Schema package in use: version 27.1.1, commit <sha>`).
* **Trying a version in a running environment**, with or without restarting the bootstrap container, is described in
  the [bootstrap module README](../../devops/core-plus/bootstrap-osdu-module/README.md#trying-another-version-in-a-running-environment).

## Trying schemas that are not released yet

Nothing is published for a merge request or any other pipeline that is not a release, so there is no package to
install. Every pipeline still builds the bundle:

* **As a file:** open the `schema-package` job of the pipeline, browse its artifacts and download
  `dist/schema-package/schema-bootstrap-bundle.tar.gz` (kept for 7 days). Extract it and run it against an environment
  as described in *Standalone: download and run* in the
  [bootstrap module README](../../devops/core-plus/bootstrap-osdu-module/README.md).
* **As an image:** the bootstrap image built by the same pipeline contains this bundle, so deploying that image uses
  these schemas.

The `manifest.json` of such a bundle has the `VERSION` of the branch and the commit, which tells builds of the same
version apart.

## GitLab setting for overwriting

The aliases `X` and `X.Y` are overwritten on every release: that uploads another file under an existing package name and
version. This works with GitLab's default for generic packages (duplicates allowed, the newest upload is served). If
the project forbids duplicate generic packages, add `^[0-9]+(\.[0-9]+){0,2}$` to the exceptions, otherwise the job
fails on the second upload of an alias. The exact `X.Y.Z` is never overwritten, so it needs no exception.

## Where it is implemented

[`deployments/scripts/schema-package.py`](../scripts/schema-package.py): the version check, the build of the bundle,
the three names, the guard, the comparison with a published `X.Y.Z` and the messages. The `schema-package` job in
[`override-stages.yml`](../../devops/core-plus/pipeline/override-stages.yml) runs it and decides by its rules whether
the pipeline is a release. The bootstrap that loads the schemas is
[`bootstrap_schema.py`](../../devops/core-plus/bootstrap-osdu-module/bootstrap_schema.py).
