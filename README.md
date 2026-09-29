# OSDU Schema Service for Azure

[![Release](https://img.shields.io/github/v/release/Azure/osdu-spi-schema)](https://github.com/Azure/osdu-spi-schema/releases)
[![Validate](https://github.com/Azure/osdu-spi-schema/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/Azure/osdu-spi-schema/actions/workflows/validate.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> [!NOTE]
> Shared service code comes from the [OSDU community upstream](https://community.opengroup.org/osdu/platform/system/schema-service).

Schema stores and serves the schemas that define every OSDU record type, both the shared schemas every partition sees and the private schemas a single partition registers.

## At a glance

| | |
|---|---|
| API base path | `/api/schema-service/v1/` |
| Swagger UI | `/api/schema-service/v1/swagger` |
| Health | `:8081/actuator/health` |
| Depends on | Partition, Entitlements |
| Azure resources | Cosmos DB (schema metadata, plus the system database for shared schemas), Storage (schema documents), Service Bus (`schemachangedtopic` topic) |
| Deployed by | [OSDU SPI Stack](https://github.com/Azure/osdu-spi-stack) (`software/stacks/osdu/services/schema.yaml`, `software/stacks/osdu/schema-load/`) |

## Repository layout

[CONTRIBUTING.md](CONTRIBUTING.md) explains where each kind of change belongs.

| Path | Owner | Contents |
|---|---|---|
| `schema-core/` | OSDU upstream | Shared service code |
| `provider/schema-azure/` | This repository | Azure provider |
| `schema-acceptance-test/` | OSDU upstream | End-to-end suite run against a deployed environment |
| `deployments/shared-schemas/` | OSDU upstream | The shared schemas the loader image publishes |
| `testing/schema-test-azure/` | This repository | Legacy Azure integration tests |
| `.spi/service.yaml` | This repository | How CI deploys and tests the service on SPI Stack |

## Build

Requires Java 17 and Maven 3.8+. OSDU dependencies resolve from the public community registry through the settings file in `.mvn`:

```bash
mvn --settings .mvn/community-maven.settings.xml -P core,azure clean install
```

The runnable jar lands at `provider/schema-azure/target/os-schema-azure-*-spring-boot.jar`.

## Configuration

SPI Stack sets the service's environment from two places: the shared `osdu-config` ConfigMap and the service's own entry in [`services/schema.yaml`](https://github.com/Azure/osdu-spi-stack/blob/main/software/stacks/osdu/services/schema.yaml). Those files are the contract; the tables below list what Schema actually reads from them.

**Shared, from `osdu-config`:**

| Variable | Purpose |
|---|---|
| `AZURE_TENANT_ID` | Entra tenant |
| `AAD_CLIENT_ID` | Application ID that caller tokens are issued for |
| `KEYVAULT_URI` | Central Key Vault |
| `APPINSIGHTS_KEY` | Telemetry |

**Specific to Schema**, from `services/schema.yaml`:

| Variable | Value on SPI Stack | Purpose |
|---|---|---|
| `SERVER_SERVLET_CONTEXTPATH` | `/api/schema-service/v1/` | API base path |
| `AZURE_ISTIOAUTH_ENABLED` | `true` | Trust the mesh's token validation |
| `AZURE_PAAS_WORKLOADIDENTITY_ISENABLED` | `true` | Authenticate to Azure with workload identity |
| `SERVER_PORT` | `8080` | HTTP port |
| `PARTITION_SERVICE_ENDPOINT` | `http://partition/api/partition/v1` | Per-partition resource lookup |
| `ENTITLEMENTS_SERVICE_ENDPOINT` | `http://entitlements/api/entitlements/v2` | Caller authorization |
| `ENTITLEMENTS_SERVICE_API_KEY` | `OBSOLETE` | Legacy API key passed to the Entitlements client; SPI Stack sets a placeholder, and the property has no default, so it must be set |
| `COSMOSDB_DATABASE` | `osdu-db` | Database inside each partition's Cosmos DB account |
| `AZURE_SYSTEM_STORAGECONTAINERNAME` | `system` | Container in the system storage account for shared schemas |
| `SERVICE_BUS_ENABLED` | `true` | Publish schema change events to Service Bus |
| `SERVICEBUS_TOPIC_NAME` | `schemachangedtopic` | Topic for schema change events |
| `EVENT_GRID_ENABLED` | `false` | Event Grid publishing is off on SPI Stack |
| `EVENT_GRID_TOPIC` | `schemachangedtopic` | Resolved at startup even while Event Grid is off |

The service authenticates to Azure with workload identity, which injects `AZURE_CLIENT_ID` and a federated token; there are no client secrets. Per-partition resources are resolved at request time through the Partition service. Shared schemas live in the system Cosmos DB database `osdu-system-db` and the system storage account, whose endpoints come from the Key Vault secrets `system-cosmos-endpoint` and `system-storage`.

## Test

| Suite | Where | Runs in CI | Run it yourself |
|---|---|---|---|
| Unit | `schema-core`, `provider/schema-azure` | Pull requests (Java Build) | `mvn ... install` from [Build](#build) |
| Acceptance | [`schema-acceptance-test`](schema-acceptance-test/README.md) | Pull requests, against SPI Stack (Deploy and Test) | `spi test schema` |
| Integration | `testing/schema-test-core`, `testing/schema-test-azure` | No | See below |

CI runs these on pull requests from this repository that change code. Documentation-only changes skip the build, and pull requests from forks build without deploying.

**Acceptance** proves a change on real infrastructure before it merges. It calls the deployed service through the gateway as a privileged test identity, and the bindings in `.spi/service.yaml` supply its host, partition, shared tenant, and token. Against an environment you are connected to:

```bash
spi test schema                   # the image and suite the environment is running
spi test schema --source .        # this checkout's suite and descriptor
```

**Integration** is the older suite carried from upstream. It sits outside the root Maven build and CI does not run it. It accepts a bearer token in `INTEGRATION_TESTER_ACCESS_TOKEN`, so `spi token` can supply one, but it has not been proven against SPI Stack. Acceptance covers the same API surface.

To call the API by hand, `spi token` mints a bearer token:

```bash
curl -H "Authorization: Bearer $(spi token)" -H "data-partition-id: <partition>" \
  https://<gateway>/api/schema-service/v1/schema?limit=10
```

## Deploy

For a pull request from this repository that changes code, CI publishes three images to GHCR: the service, `osdu-spi-schema`; its test suite, `osdu-spi-schema-acceptance`; and its loader, `osdu-spi-schema-load`, built from `build/load.Dockerfile` with the schemas in `deployments/shared-schemas/`. The Deploy and Test lane then borrows an SPI Stack environment, pins the service and loader from the same commit, proves them with the acceptance suite, and restores the environment's own images. When that lane runs and passes, the change is proven on real infrastructure before it merges; the Validation Summary on the pull request shows whether it ran. This repository does not own infrastructure; SPI Stack does.

To try a build by hand on an environment you are connected to, pin it by digest and release the pin when done:

```bash
spi service pin schema --image ghcr.io/azure/osdu-spi-schema@sha256:<digest>
spi service reset schema
```

A pin made this way moves only the service. The loader is paired only on the ephemeral pins CI makes, and `schema-load` cannot be pinned on its own.

## Service notes

**Shared schemas and the loader.** SPI Stack runs a one-shot `schema-load` Job that waits for the service, then publishes the shared schemas into the primary partition through the API. The loader image must come from the same commit as the service so the schemas match the code serving them, which is why CI pairs the two. To rerun the load on an environment, delete the Job and let Flux recreate it: `kubectl delete job schema-load -n osdu`.

**Event Grid and Service Bus.** The provider can publish schema change events to either. SPI Stack turns Service Bus on and Event Grid off; `EVENT_GRID_TOPIC` is still required, though unused while Event Grid is off.

## License

Copyright © Microsoft Corporation

Licensed under the [Apache License 2.0](LICENSE).
