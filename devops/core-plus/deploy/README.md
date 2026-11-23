<!--- Deploy --->

# Deploy helm chart

## Introduction

This chart installs a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6)
> It is possible to use other versions, but it hasn't been tested

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

* **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)
* **Kubectl** (version: v1.21.0 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Global variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**global.domain** | your domain, ex `example.com`. Not used by this chart, but may be used in different deployment setups. Use `global.hostname` for the route host | string | - | no
**global.hostname** | host the `HTTPRoute` attaches to when `httpRoute.hostnames` is empty, ex `osdu.example.com` | string | `""` | only if `httpRoute.enabled` is true and `httpRoute.hostnames` is empty
**global.limitsEnabled** | whether CPU and memory limits are enabled | boolean | true | yes
**global.dataPartitionId** | data partition ID (used as shared tenant name) | string | - | yes

### Configmap variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.entitlementsHost** | entitlements host | string | "http://entitlements" | yes
**data.javaOptions** | java options | string | "-Xms512M -XX:MaxRAMPercentage=70 -XX:+UseG1GC -XX:+UseStringDeduplication -XX:InitiatingHeapOccupancyPercent=45" | yes
**data.logLevel** | logging level | string | ERROR | yes
**data.partitionHost** | partition host | string | "http://partition" | yes
**data.schemaTopicName** | topic for schema changes events | string | "schema-changed" | yes
**data.schemaChangedMessagingEnabled** | whether schema changed messaging is enabled | boolean | true | yes
**data.enableCleanup** | whether schema cleanup is enabled | boolean | false | yes
**data.schemaHost** | schema host | string | "http://schema" | yes

### Deployment variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.requestsCpu** | amount of requested CPU | string | 220m | yes
**data.requestsMemory** | amount of requested memory| string | 1.7G | yes
**data.limitsCpu** | CPU limit | string | 1 | only if `global.limitsEnabled` is true
**data.limitsMemory** | memory limit | string | 2.5G | only if `global.limitsEnabled` is true
**data.bootstrapImage** | bootstrap image | string | - | yes
**data.bootstrapServiceAccountName** | bootstrap service account name | string | - | yes
**data.schemaAuthority** | authority the bootstrap job registers the shared schemas under, the first part of a kind (`osdu` in `osdu:wks:...`); only letters, digits, `_` and `-` | string | `osdu` | no
**data.bootstrapWorkers** | threads used by the bootstrap job to register schemas, from 1 (sequential) to 8 | integer | 4 | no
**data.schemaPackageVersion** | version of the bootstrap bundle whose schemas are downloaded from the GitLab package registry instead of using the schemas bundled in the bootstrap image, ex `27.1.0`, `27.1` (newest 27.1.x), `27` (newest 27.x.x); write it in quotes in the values file, a number is rejected | string | "" (use the bundled schemas) | no
**data.schemaPackageUrl** | base URL of the generic package registry used for the download; empty uses the URL recorded in the bootstrap image | string | "" | no
**data.schemaPackageSha256** | optional sha256 of the downloaded bundle file, as GitLab shows it in the package's Assets | string | "" | no
**data.schemaPackageFallback** | use the bundled schemas if the schema package download fails, instead of failing the bootstrap job | boolean | false | no
**data.image** | service image | string | - | yes
**data.imagePullPolicy** | when to pull image | string | IfNotPresent | yes
**data.serviceAccountName** | name of your service account | string | schema | yes

### Bootstrap schema loading

The bootstrap job registers the shared OSDU schemas with the Schema service. The bootstrap image
(`data.bootstrapImage`) is built from a versioned bootstrap bundle (code, pinned libraries and schemas), and the same
bundle is published to the GitLab package registry. By default the job uses the schemas of the image and needs no
network access to the registry.

* To load the schemas of another published version without rebuilding the image, set `data.schemaPackageVersion`.
  The job downloads that bundle, checks it against `data.schemaPackageSha256` (if set), extracts only its schemas
  and uses them instead of the bundled ones. If the download fails the job fails,
  unless `data.schemaPackageFallback` is `true`.
  If the registry is not readable anonymously (for example a private project), add a `SCHEMA_PACKAGE_TOKEN` key to the
  secret named by `conf.bootstrapSecretName`; see
  [Loading schemas from a private GitLab project](../bootstrap-osdu-module/README.md#loading-schemas-from-a-private-gitlab-project).
* `data.bootstrapWorkers` (default `4`, maximum `8`) sets how many threads register schemas. The abstract schemas are
  registered first, one at a time, then independent schemas in parallel. Set it to `1` to register strictly one schema
  after another. More threads do not always help: in a test on one environment 4 threads was the fastest, because
  Postgres ran out of CPU.
* `data.schemaAuthority` (default `osdu`) is the authority the schemas are registered under, so the kinds become
  `<authority>:wks:...`. A value other than `osdu` creates a second set of schemas next to any existing ones; see
  [Registering under another authority](../bootstrap-osdu-module/README.md#registering-under-another-authority).

Example:

```console
helm upgrade --install core-plus-schema-deploy . \
  --set data.bootstrapWorkers=2 \
  --set data.schemaAuthority=acme \
  --set-string data.schemaPackageVersion=27.1.0
```

See the [bootstrap module README](../bootstrap-osdu-module/README.md) for the package versions and the environment
variables behind these values.

### Configuration variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**conf.appName** | name of the app | string | `schema` | yes
**conf.bootstrapSecretName** | secret for bootstrap | string | `datafier-secret` | yes
**conf.configmap** | configmap to be used | string | `schema-config` | yes
**conf.s3SecretName** | secret for SeaweedFS/S3 schema storage (prefixed with `global.dataPartitionId`) | string | `schema-seaweedfs-secret` | yes
**conf.postgresSecretName** | secret for postgres | string | `schema-postgres-secret` | yes
**conf.rabbitmqSecretName** | secret for rabbitmq | string | `rabbitmq-secret` | yes
**conf.replicas** | number of deployment replicas | integer | 1 | yes

### Istio variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | `90m` | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | `500m` | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | `100Mi` | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | `512Mi` | yes
**istio.bootstrapProxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes
**istio.bootstrapProxyCPULimit** | CPU limit for Envoy sidecars | string | `100m` | yes

### Routing (HTTPRoute)

The chart owns the routing for the service. It renders a Gateway API `HTTPRoute`, disabled by default. The chart no longer ships an Istio `VirtualService`.

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|----------|
**httpRoute.enabled** | render the `HTTPRoute` | boolean | `false` | yes
**httpRoute.name** | route name. When empty the name is `<release>-route`, which differs from any route of the same name defined outside this chart | string | `""` | no
**httpRoute.parentRefs** | gateways the route attaches to | list | `cimpl-gateway` in `istio-system` | yes
**httpRoute.hostnames** | hosts the route serves. When empty, `global.hostname` is used | list | `[]` | one of `httpRoute.hostnames` or `global.hostname`
**httpRoute.routes** | one rule per entry, see below | list | `/api/schema-service/` | yes

The render fails when `httpRoute.enabled` is true and both `httpRoute.hostnames` and `global.hostname` are empty, so the route is never exposed on all hosts.

Each entry of `httpRoute.routes` accepts:

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|----------|
**path** | path to match | string | - | yes
**pathType** | match type, `PathPrefix` or `Exact` | string | `PathPrefix` | no
**serviceName** | backend Service name | string | `conf.appName` | no
**port** | backend Service port | integer | `80` | no
**filters** | Gateway API filters, for example `URLRewrite` | list | - | no

Example, a route with a rewrite and another port:

```yaml
httpRoute:
  enabled: true
  hostnames:
    - osdu.example.com
  routes:
    - path: /api/schema-service/
    - path: /other/
      port: 9002
      filters:
        - type: URLRewrite
          urlRewrite:
            path:
              type: ReplacePrefixMatch
              replacePrefixMatch: /
```

### Install the helm chart

Run this command from within this directory:

```console
helm install core-plus-schema-deploy .
```

To expose the service through the Gateway API:

```console
helm install core-plus-schema-deploy . --set httpRoute.enabled=true --set global.hostname=osdu.example.com
```

To check the rendered route without installing:

```console
helm template core-plus-schema-deploy . --set httpRoute.enabled=true --set global.hostname=osdu.example.com -s templates/httproute.yaml
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall core-plus-schema-deploy
```

[Move-to-Top](#deploy-helm-chart)
