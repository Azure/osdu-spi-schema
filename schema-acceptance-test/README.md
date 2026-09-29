### Running E2E Tests

You will need to have the following environment variables defined.

| name              | value                                              | description                                      | sensitive? | source | required |
|-------------------|----------------------------------------------------|--------------------------------------------------|------------|--------|----------|
| `HOST`            | ex `http://localhost:8080`                         | Schema service base URL (no trailing slash)      | no         | -      | yes      |
| `DATA_PARTITION_ID` | ex `opendes`                                     | Default data partition for tests                 | no         | -      | yes      |
| `SHARED_TENANT`   | ex `system`                                        | Shared/system tenant partition                   | no         | -      | no       |

Authentication can be provided as OIDC config:

| name                                                   | value                                      | description                                          | sensitive? | source |
|--------------------------------------------------------|--------------------------------------------|------------------------------------------------------|------------|--------|
| `TEST_OPENID_PROVIDER_URL`                             | ex `https://keycloak.com/auth/realms/osdu` | OpenID provider discovery URL (shared across users)  | yes        | -      |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_ID`            | `********`                                 | OIDC client ID for the privileged test user          | yes        | -      |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_SECRET`        | `********`                                 | OIDC client secret for the privileged test user      | yes        | -      |
| `PRIVILEGED_USER_OPENID_PROVIDER_SCOPE`                | ex `api://my-app/.default`                 | OAuth2 scope (optional, defaults to `openid`)        | no         | -      |

Or a pre-configured bearer token can be used directly:

| name                    | value      | description                                           | sensitive? | source |
|-------------------------|------------|-------------------------------------------------------|------------|--------|
| `PRIVILEGED_USER_TOKEN` | `********` | Bearer token for the privileged test user (optional)  | yes        | -      |

When `PRIVILEGED_USER_TOKEN` is set it is used as-is and OIDC variables are ignored.

**Entitlements configuration for integration accounts**

| PRIVILEGED_USER                     |
|-------------------------------------|
| users                               |
| service.schema-service.system-admin |
| service.entitlements.user           |
| service.schema-service.viewers      |
| service.schema-service.editors      |
| data.integration.test               |
| data.test1                          |

**Test execution phases**

Tests are executed in three Maven Failsafe phases:

1. `pre-integration-test` — runs `PreIntegrationTestsRunner` (`@Startup` tag): verifies the service `/info` endpoint is reachable before any test data is created
2. `integration-test` — runs `SchemaServiceTestsRunner` (`@SchemaService` tag): all schema CRUD acceptance tests
3. `post-integration-test` — runs `TearDownTestsRunner` (`@TearDown` tag): teardown after the test run

Execute the following command to build and run all acceptance tests:

```bash
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
cd schema-acceptance-test && mvn clean verify
```

## License

Copyright © Google LLC

Copyright © EPAM Systems

Copyright © ExxonMobil

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
