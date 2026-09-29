import dataclasses
import logging
import os

logger = logging.getLogger()

DEFAULT_DATASTORE_NAMESPACE = "dataecosystem"
DEFAULT_DATASTORE_KIND = "system_schema_osm_testing"


@dataclasses.dataclass(frozen=True)
class SchemaConfig:
    schema_bucket_name: str
    data_partition_id: str
    datastore_namespace: str
    datastore_kind: str


def get_schema_config_from_env() -> SchemaConfig:
    try:
        schema_bucket_name = os.environ["SCHEMA_BUCKET"]
        data_partition_id = os.environ["DATA_PARTITION"]
    except KeyError as err:
        logger.error(
            "You must specify the following env variables: 'SCHEMA_BUCKET' and 'DATA_PARTITION'")
        raise err
    datastore_namespace = os.environ.get("DATASTORE_NAMESPACE")
    datastore_kind = os.environ.get("DATASTORE_KIND")
    if not datastore_namespace:
        datastore_namespace = DEFAULT_DATASTORE_NAMESPACE
        logger.warning(f"Default Datastore namespace '{DEFAULT_DATASTORE_NAMESPACE}' is used.")

    if not datastore_kind:
        datastore_kind = DEFAULT_DATASTORE_KIND
        logger.warning(f"Default Datastore kind '{DEFAULT_DATASTORE_KIND}' is used.")

    return SchemaConfig(
        schema_bucket_name=schema_bucket_name,
        data_partition_id=data_partition_id,
        datastore_namespace=datastore_namespace,
        datastore_kind=datastore_kind
    )
