# Shared Schemas

The purpose of this folder set is to contain schema definitions in a state ready to
register with the **Schema Service**. Each schema version will have its own file,
grouped together with all parallel versions under a folder carrying the entity name.
Example `<schema-authority>/<group-type-folder>/entity-schema-version.json`  

The deployment pipeline will only deploy pre-processed schemas in this `shared-schemas`
folder. The script to do this is [DeploySharedSchemas.py](../scripts/DeploySharedSchemas.py), see
step **Upload schema definitions** below. The pre-processed schemas are produced by
OSDU Data Definitions
(see [](https://gitlab.opengroup.org/osdu/subcommittees/data-def/work-products/schema/-/tree/master)).

The structure of JSON files to register matches the expected payload of the Schema Service
POST/PUT requests:

```json
{
  "schemaInfo": {
    "schemaIdentity": {
      "authority": "{{schema-authority}}",
      "source": "wks",
      "entityType": "work-product-component--WellLog",
      "schemaVersionMajor": 1,
      "schemaVersionMinor": 0,
      "schemaVersionPatch": 0,
      "id": "{{schema-authority}}:wks:work-product-component--WellLog:1.0.0"
    },
    "createdBy": "OSDU Data Definition Group",
    "scope": "SHARED",
    "status": "DEVELOPMENT"
  },
  "schema": {
  }
}
```

The `"schema"` property carries the full schema definition - omitted in the above example.

Schemas may refer to abstract entity definitions or other external schema fragments. The
Schema Service requires the abstract definitions and schema fragments to be registered prior
to the registration of the main entity schema. This is achieved by a file defining the
load sequence per schema version. An example can be found
[here for OSDU R3](../shared-schemas/osdu/load_sequence.1.0.0.json).

## Upload schema definitions

Once the loading instructions are completed, the schema registration can be launched. this is
done via the [DeploySharedSchemas.py](../scripts/DeploySharedSchemas.py). Important parameters, i.e.
the target schema authority and the path to the load-sequence file are in code in `DEFAULT_BOOTSTRAP_OPTIONS`
[Utility.py](../scripts/Utility.py#L18). The DeploySharedSchemas.py options are as follows:

```shell script
python deployments\scripts\DeploySharedSchemas.py -h
usage: DeploySharedSchemas.py [-h] [-a A] [-l L] [-u U]

Given a path to an load sequence file, load/update the schemas listed in the
load sequence file.

optional arguments:
  -h, --help  show this help message and exit
  -u U        The complete URL to the Schema Service.


example:
python deployments\scripts\DeploySharedSchemas.py -u https://opengroup.test.org/api/schema-service/v1/schema
```

### Environment value need to execute Token.py script

```python
import os
JSON_KEY = os.environ.get('JSON_KEY')
```

The above snippet is from the [Token.py](../scripts/google/Token.py) script and lists the required
environment variable for json key. This value can be different as per cloud vendors token generation logic.

### Bearer Token Generation

Bearer token generation logic can differ for each cloud vendors. So, each cloud vendor can provide their implementation in below format in specific folder under scripts [google](../scripts/google/). To generate token
for google implementation below script is used in [azure pipeline](../../azure-pipelinea.yml)

```shell script
BEARER_TOKEN=`python deployments/scripts/google/Token.py`
```

We export the token generated to `BEARER_TOKEN` which is used in DeploySharedSchemas.py script

### Environment value need to execute DeploySharedSchemas.py script

```python
import os
BEARER_TOKEN = os.environ.get('BEARER_TOKEN')
APP_KEY = os.environ.get('APP_KEY')
DATA_PARTITION = os.environ.get('DATA_PARTITION')
```

The above snippet is from the [Utility.RunEnv](../scripts/Utility.py) class and lists the required
environment variables for bearer token, app key and tenant/data-partition-id.

### Yaml Pipeline configurations

```shell script
#!/bin/bash
pip install -r deployments/scripts/google/requirements.txt

export JSON_KEY=$(INTEGRATION_TESTER)

BEARER_TOKEN=`python deployments/scripts/google/Token.py`

export BEARER_TOKEN=$BEARER_TOKEN
export APP_KEY=""
export DATA_PARTITION=$(DATA_PARTITION)

python deployments/scripts/DeploySharedSchemas.py -u $(SCHEMA_DEV_URL)/schema
```

In the above script we first install all the required dependencies, then create a bearer token for the specific cloud provider and then execute DeploySharedSchema script.
Sample yaml can be in [azure pipeline](../../azure-pipelinea.yml)

### Schema Registration

The upload will depend on the status of the schemas. Schemas in `DEVELOPMENT` can be updated,
schemas in status `PUBLISHED` can only be created once (POST).

The script produces output like:

```shell script
python.exe C:/Users/gehrmann/git_repos/PyCharm/os-schema/deployments/scripts/DeploySharedSchemas.py -l load_sequence.1.0.0.json -u https://api.evq.csp.slb.com/de/schema-service/v1/schema
Success: kind osdu:osdu:DataCollection:1.0.0 submitted with method PUT schema.
Success: kind osdu:osdu:File:1.0.0 submitted with method PUT schema.
...
Success: kind osdu:osdu:WellLogWorkProductComponent:1.0.0 submitted with method PUT schema.
Success: kind osdu:osdu:WorkProduct:1.0.0 submitted with method PUT schema.
This update took 190.44 seconds.
All 120 schemas registered or updated.

```

In case of errors, the list of failed creations/updates are summarized at the end.

### Environment clean up (Google Cloud)

Schema bootstrapping used during new platform configuration, creates schema records in Datastore, which cannot be removed during deletion.
If platform deployment must be re-installed, the cleanup script must be executed.
Scripts for cleanup schemas can be found in [GCDatastoreCleanUp.py](../scripts/GCDatastoreCleanUp.py)

```bash
pip install -r gc-deployment-requirements.txt
```

You will need to have the following environment variables defined to run scripts.
| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `DATA_PARTITION` | ex `osdu`| Data partition id| no | - |
| `SHARED_PARTITION_ID` | ex `osdu`| Data partition id that will be used for deletion schemas by id `"{{SHARED_PARTITION_ID}}:wks:work-product-component--Activity:1.0.0"`| no | - |
| `SCHEMA_NAMESPACE` | ex `dataecosystem`| If not specified default `dataecosystem` will be used | no | - |
| `SCHEMA_KIND` | ex `schema`| If not specified default `schema` will be used  | no | - |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex`usr/key.json` | Google Service account credentials with delete access to Datastore | yes | - |
