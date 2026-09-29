[[_TOC_]]

## Prerequisites 
- Azure CLI 2.11.x or higher

## How to build and push a docker container
- Navigate to repo root
- Login to your azure container registry
```
az login
az account set -s "AG-IND-OSDU-TEST" #Update subscription id
az acr login -n msosdu  #Update registry name
```
- Build and push the container image
```
az acr build -r msosdu -t schema-data-init:0.12.0 -f "deployments/scripts/azure/Dockerfile" .
```
## Running the container locally
- Prepare a .env file, let's say `env_file.env` with following content:
```
AZURE_DNS_NAME=<your_dns> #e.g. osdu-dev.msft-osdu-test.org
AZURE_TENANT_ID=<tenant_id>
AZURE_AD_APP_RESOURCE_ID=<aad_client_id>
AZURE_CLIENT_ID=<client_id>
AZURE_CLIENT_SECRET=<client_secret>
```
- Run the container:
```
docker run --env-file .env msosdu.azurecr.io/schema-data-init:0.12.0
```
Ensure that image name is put at the very end of the command and all other parameters are specified before that.

## Validation
Once the container starts running, you'll see logs like this:
```
Current data-partition-id: opendes
Try POST for id: osdu:wks:AbstractAccessControlList:1.0.0
Try PUT for id: osdu:wks:AbstractAccessControlList:1.0.0
The kind osdu:wks:AbstractAccessControlList:1.0.0 was registered successfully.
Try POST for id: osdu:wks:AbstractActivityParameter:1.0.0
Try PUT for id: osdu:wks:AbstractActivityParameter:1.0.0
The kind osdu:wks:AbstractActivityParameter:1.0.0 was registered successfully.
...
```