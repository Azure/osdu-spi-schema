## Documentation
Official documentation [https://osdu.pages.opengroup.org/platform/system/schema-service/](https://osdu.pages.opengroup.org/platform/system/schema-service/)

## Running the schema service locally

The Schema Service is a Maven multi-module project with each cloud implemention placed in its submodule. To build or run Schema Service locally, follow the below steps :

## 1. Google Cloud deployment

Instructions for running the Google Cloud implementation in the cloud can be found [here](./provider/schema-gc/README.md).

## 2. Azure deployment

Instructions for running the Azure implementation in the cloud can be found [here](https://community.opengroup.org/osdu/platform/system/schema-service/-/blob/master/provider/schema-azure/README.md).

## Running Automated Integration Test

DevSanity tests are located in a schema-core project in testing directory under the project root directory.

1. Google Cloud
These tests validate functionality of schema service.

They can then be run/debugged directly in your IDE of choice using the GUI or via the commandline using below command from schema-core project.
Below command has to be run post building complete project.

Instructions for running the Google Cloud integration tests can be found [here](./provider/schema-gc/README.md).

Below command can be run through azure-pipeline.yml after setting environment variables in the pipeline.

 verify
 
## Deploy Shared Schemas

Schema service as part of deployment deploys pre-defined OSDU schemas so end users can get community accepted schemas to refer. Such schemas are present in [folder](./deployments/shared-schemas/osdu) and script to deploy the schema are present [here](deployments/scripts).

Details to deploy shared schemas can be found under [README.md](./deployments/shared-schemas/README.md)
  
## AWS

The AWS provider has been removed from this repository.

## Open API 3.0 - Swagger
- Swagger UI : https://host/context-path/swagger (will redirect to https://host/context-path/swagger-ui/index.html)
- api-docs (JSON) : https://host/context-path/api-docs
- api-docs (YAML) : https://host/context-path/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](./schema-core/src/main/resources/swagger.properties)

#### Server Url(full path vs relative path) configuration
- `api.server.fullUrl.enabled=true` It will generate full server url in the OpenAPI swagger
- `api.server.fullUrl.enabled=false` It will generate only the contextPath only
- default value is false (Currently only in Azure it is enabled)
[Reference]:(https://springdoc.org/faq.html#_how_is_server_url_generated)

## License

Copyright 2017-2020, Schlumberger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License
