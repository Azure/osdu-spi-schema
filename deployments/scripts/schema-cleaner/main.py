from schemas_cleaner import SchemasCleaner
from utils import get_schema_config_from_env

def main():
    schema_config = get_schema_config_from_env()
    schemas_cleaner = SchemasCleaner(schema_config)
    schemas_cleaner.clean_broken_schemas()

if __name__ == "__main__":
    main()
