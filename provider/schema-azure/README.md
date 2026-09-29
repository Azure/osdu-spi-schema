## Schema Service

[![coverage report](https://community.opengroup.org/osdu/platform/system/schema-service/badges/master/coverage.svg)](https://community.opengroup.org/osdu/platform/system/schema-service/-/commits/master)

## Running Locally

### Requirements

In order to run this service locally, you will need the following:

- [Maven 3.8.0+](https://maven.apache.org/download.cgi)
- [Java 17](https://adoptopenjdk.net/)
- Download the [application-insights-agent](https://github.com/microsoft/ApplicationInsights-Java/releases/tag/3.5.2) jar
- Infrastructure dependencies, deployable through the relevant [infrastructure template](https://dev.azure.com/slb-des-ext-collaboration/open-data-ecosystem/_git/infrastructure-templates?path=%2Finfra&version=GBmaster&_a=contents)
- While not a strict dependency, example commands in this document use [bash](https://www.gnu.org/software/bash/)

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)


### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

**Required to run service**

| name                                             | value                                                               | description                                                                                                        | sensitive? | source                                                                 |
|--------------------------------------------------|---------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|------------|------------------------------------------------------------------------|
| `LOG_PREFIX`                                     | `schema`                                                            | Logging prefix                                                                                                     | no         | -                                                                      |
| `entitlements_service_endpoint`                  | ex `https://foo-entitlements.azurewebsites.net/api/entitlements/v2` | Entitlements API endpoint                                                                                          | no         | output of infrastructure deployment                                    |
| `entitlements_service_api_key`                   | `OBSOLETE`                                                          | The API key clients will need to use when calling the entitlements                                                 | yes        | --                                                                     |
| `partition_service_endpoint`                     | ex `https//foo-partition.azurewebsites.net/api/partition/v1`        | Partition Service API endpoint                                                                                     | no         | output of infrastructure deployment                                    |
| `azure.activedirectory.app-resource-id`          | `********`                                                          | AAD client application ID                                                                                          | yes        | keyvault secret: `$KEYVAULT_URI/secrets/aad_client_id`                 |
| `azure.application-insights.instrumentation-key` | `********`                                                          | API Key for App Insights                                                                                           | yes        | keyvault secret: `$KEYVAULT_URI/secrets/appinsights-key`               |
| `APPLICATIONINSIGHTS_CONNECTION_STRING`          | `InstrumentationKey=${appinsights_key}`                             | Connection String for App Insights. Instrumentation Key value can be obtained from Azure portal or from Key Vault  | yes        | keyvault secret: `$KEYVAULT_URI/secrets/appinsights-connection-string` |
| `azure.activedirectory.client-id`                | `********`                                                          | AAD client application ID                                                                                          | yes        | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username`           |
| `azure.activedirectory.AppIdUri`                 | `api://${azure.activedirectory.client-id}`                          | URI for AAD Application                                                                                            | no         | --                                                                     |
| `azure.activedirectory.session-stateless`        | `true`                                                              | Flag run in stateless mode (needed by AAD dependency)                                                              | no         | --                                                                     |
| `azure.storage.account-name`                     | ex `foo-storage-account`                                            | Storage account for storing documents                                                                              | no         | output of infrastructure deployment                                    |
| `cosmosdb_database`                              | `osdu-db`                                                           | Cosmos database                                                                                                    | no         | --                                                                     |
| `event_grid_enabled`                             | ex `true`                                                           | Indicates whether event grid is enabled or not                                                                     | no         | if env is demo then value is `false`, otherwise it is `true`           |
| `event_grid_topic`                               | `schemachangedtopic`                                                | Event grid topic name                                                                                              | no         | --                                                                     |
| `service_bus_enabled`                            | ex `false`                                                          | Indicates whether service bus is enabled or not                                                                    | no         | if env is demo then value is `true`, otherwise it is `false`           |
| `servicebus_topic_name`                          | `schemachangedtopic`                                                | Service bus topic name                                                                                             | no         | --                                                                     |
| `KEYVAULT_URI`                                   | ex `https://foo-keyvault.vault.azure.net/`                          | URI of KeyVault that holds application secrets                                                                     | no         | output of infrastructure deployment (central resources kv)             |
| `AZURE_CLIENT_ID`                                | `********`                                                          | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes        | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username`           |
| `AZURE_TENANT_ID`                                | `********`                                                          | AD tenant to authenticate users from                                                                               | yes        | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-tenant-id`          |
| `AZURE_CLIENT_SECRET`                            | `********`                                                          | Secret for `$AZURE_CLIENT_ID`                                                                                      | yes        | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password`           |
| `azure_istioauth_enabled`                        | `true`                                                              | Flag to Disable AAD auth                                                                                           | no         | --                                                                     |
| `azure_localhosttesting_auth_enabled`            | `false`                                                             | Flag to Enable AAD auth for localhost testing                                                                                        | no         | --                                                                     |
| `shared_partition`                               | `opendes`                                                           | Default Partition for Public Shared Schemas                                                                        | no         | --                                                                     |
| `server.port`                                    | ex `8085`                                                           | port for schema service                                                                                            | no         | --                                                                     |


**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `AZURE_AD_APP_RESOURCE_ID` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `AZURE_AD_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | -- |
| `INTEGRATION_TESTER` | `********` | System identity to assume for API calls. Note: this user must have entitlements configured already | no | -- |
| `PRIVATE_TENANT1` | `opendes` | OSDU tenant used for testing | no | -- |
| `PRIVATE_TENANT2` | `tenant2` | OSDU tenant used for testing | no | -- |
| `SHARED_TENANT` | `common` | OSDU tenant used for testing | no | -- |
| `VENDOR` | `azure` | cloud provider name | no | -- |
| `HOST` | ex: `http://localhost:8080` | local service endpoint | no | -- |
| `TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$INTEGRATION_TESTER` | yes | -- |

### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You will need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.m2/settings.xml`:
```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>os-core</id>
            <username>mvn-pat</username>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
            <password>$PERSONAL_ACCESS_TOKEN_GOES_HERE</password>
        </server>
    </servers>
</settings>
```

### Build and run the application

After configuring your environment as specified above, you can follow these steps to build and run the application.
1. Navigate to the root of the schema project, os-schema. For building the project from the command line, run the command below:
    ```bash
    mvn -P core,aws,azure,ibm,gc clean install
    ```
    This will build the core profile as well as all underlying profiles. If we want to build the profiles for a specific cloud vendor, we will build both the core profile and the cloud vendor's profile. For example, if we want to build the profiles for Azure, we can use the command below:
    ```bash
    mvn -P core,azure clean install
    ```
2. Run schema service in command line. We need to select which cloud vendor specific schema-service we want to run. For example, if we want to run schema-service for Azure, run the below command :
    ```bash
    # Running Azure :
    java -jar  provider/schema-azure/target/os-schema-azure-0.0.1-SNAPSHOT-spring-boot.jar --add-opens java.base/java.lang=ALL-UNNAMED --add-opens  java.base/java.lang.reflect=ALL-UNNAMED -javaagent:<<Absolute file path to application-insights-agent jar>> -DAPPINSIGHTS_LOGGING_ENABLED=true
3. The port and path for the service endpoint can be configured in ```application.properties``` in the provider folder as following. If not specified, then  the web container (ex. Tomcat) default is used:
    ```bash
    server.servlet.contextPath=/api/schema-service/v1/
    server.port=8080
    ```


### Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/api/schema-service/v1/swagger](http://localhost:8080/api/schema-service/v1/swagger). If the request does not fail, you can then run the integration tests.

They can then be run/debugged directly in your IDE of choice using the GUI or via the commandline using below command from schema-core project.

```bash
# build + run Azure integration tests.
#
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/schema-test-core && mvn clean verify)
```

Additionally if you were trying to isolate specific variables the following can be executed

```bash
cd testing/schema-test-core
    mvn verify -DVENDOR=azure -DHOST=http://localhost:8080 -DPRIVATE_TENANT1=opendes -DPRIVATE_TENANT2=tenant2 -DSHARED_TENANT=common -Dcucumber.options="--tags @SchemaService"
```

Below command can be run through azure-pipeline.yml after setting environment variables in the pipeline.

	verify "-Dcucumber.options=--tags @SchemaService"

## Open API 3.0 - Swagger
- Swagger UI:  http://localhost:8080/api/schema-service/v1/swagger (will redirect to  http://localhost:8080/api/schema-service/v1/swagger-ui/index.html)
- api-docs (JSON) :  http://localhost:8080/api/schema-service/v1/api-docs
- api-docs (YAML) :  http://localhost:8080/api/schema-service/v1/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](../../schema-core/src/main/resources/swagger.properties)


## Debugging

Jet Brains - the authors of Intellij IDEA, have written an [excellent guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) on how to debug java programs.


## Deploying service to Azure

Service deployments into Azure are standardized to make the process the same for all services. The steps to deploy into
Azure can be [found here](https://dev.azure.com/slb-des-ext-collaboration/open-data-ecosystem/_git/infrastructure-templates?path=%2Fdocs%2Fosdu%2FSERVICE_DEPLOYMENTS.md&_a=preview)


## License
Copyright © Microsoft Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
