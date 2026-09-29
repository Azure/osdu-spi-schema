from itertools import islice
from typing import List, Set
from xml.dom.minidom import Entity
from google.cloud import datastore
from google.cloud import storage
from utils import SchemaConfig

class SchemasCleaner:

    def __init__(self, schema_config: SchemaConfig) -> None:
        self.schema_config = schema_config
        self.datastore_client = datastore.Client(namespace=self.schema_config.datastore_namespace)
        self.storage_client = storage.Client()

    def _get_datastore_broken_schemas(self, bucket_schemas: Set[str]) -> List[datastore.Entity]:
        broken_schemas = [] 
        query = self.datastore_client.query(kind=self.schema_config.datastore_kind)
        query_iter = query.fetch()
        for entity in query_iter:
            if entity.key.name not in bucket_schemas:
                broken_schemas.append(entity)
        return broken_schemas

    def _get_bucket_names(self) -> Set[str]:
        schema_names = set()
        bucket = self.storage_client.bucket(self.schema_config.schema_bucket_name)
        for b in bucket.list_blobs():
            schema_names.add(b.name.replace(".json", ""))
        return schema_names

    def _cleanup_datastore(self, broken_schemas: List[Entity]):
        broken_schemas = iter(broken_schemas)
        while True:
            schema_batch = list(islice(broken_schemas, 400))
            print(f"Removing {len(schema_batch)} records from Datastore")
            if not schema_batch:
                break
            self.datastore_client.delete_multi(schema_batch)

    def clean_broken_schemas(self):
        bucket_schemas = self._get_bucket_names()
        broken_schemas = self._get_datastore_broken_schemas(bucket_schemas)
        self._cleanup_datastore(broken_schemas)
