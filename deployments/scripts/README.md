# Deployment

## Deploy Shared Schemas

Since the schemas are shared they are supposed to be available across all the data partitions. This means that we should use the system `v1/schemas/system` endpoint without `data-partition-id` header for uploading the schemas to Schema Service

```bash
python DeploySharedSchemas.py -u <schema-base-url>/v1/schemas/system
```

Also, you may want to exit early if any error, other than `ALREADY_PUBLISHED` or `SCHEMA_EXISTS`, occurs (e.g., 500 or 401 status codes), then you need to add `-e` flag to the cli command:

```bash
python DeploySharedSchemas.py -u <schema-base-url>/v1/schemas/system -e
```
