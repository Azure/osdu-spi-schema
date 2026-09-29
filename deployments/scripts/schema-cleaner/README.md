This script is used for cleaning Datastore Schema Entities if they are not present in Schema bucket


# Environmental variables

SCHEMA_BUCKET - name of the bucket with schemas, e.g __some-project-schemas__
DATA_PARTITION - data-partition-id
DATASTORE_NAMESPACE - Datastore Namespace for Schema, default: __dataecosystem__
DATASTORE_KIND - Datastore Kind for Schema, default: __system_schema_osm__
