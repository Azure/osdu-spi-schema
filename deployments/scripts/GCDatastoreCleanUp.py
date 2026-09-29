import json
import os
from google.api_core.retry import Retry
from google.cloud import datastore

from Utility import Utility, RunEnv

schema_namespace = os.environ.get("SCHEMA_NAMESPACE")
schema_kind = os.environ.get("SCHEMA_KIND")
shared_partition_id = os.environ.get("SHARED_PARTITION_ID", default="osdu")
default_namespace = "dataecosystem"
default_kind = "system_schema_osm"


def cleanup_datastore():
  if schema_namespace is None:
    datastore_client = datastore.Client(namespace=default_namespace)
    print(
        "SCHEMA_NAMESPACE is empty, using default namespace: " + default_namespace)
  else:
    datastore_client = datastore.Client(namespace=schema_namespace)
    print("SCHEMA_NAMESPACE not empty, using var value: " + schema_namespace)

  if schema_kind is None:
    print("SCHEMA_KIND is empty, using default kind: " + default_kind)
    kind_to_use = default_kind
  else:
    print("SCHEMA_KIND not empty, using var value: " + schema_kind)
    kind_to_use = schema_kind

  deployments = Utility.path_to_deployments()
  bootstrap_options = json.loads(RunEnv.BOOTSTRAP_OPTIONS)

  for option in bootstrap_options:
    schema_path = option['folder']
    load_sequence = option['load-sequence']
    path = os.path.join(deployments, RunEnv.SCHEMAS_FOLDER, schema_path,
                        load_sequence)
    print("Schemas sequence location: " + path)
    sequence = Utility.load_json(path)
    for item in sequence:
      complete_key = datastore_client.key(kind_to_use, item['kind'].replace(
          '{{schema-authority}}', shared_partition_id))
      print("Key to delete: " + complete_key.__str__())
      range = Retry(initial=5, maximum=20, multiplier=2, deadline=120)
      datastore_client.delete(key=complete_key, retry=range)


if __name__ == '__main__':
  cleanup_datastore()
