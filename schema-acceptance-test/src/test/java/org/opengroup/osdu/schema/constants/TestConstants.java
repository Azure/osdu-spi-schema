package org.opengroup.osdu.schema.constants;

import org.opengroup.osdu.core.test.config.EnvLoader;

public class TestConstants {
    public static final String FORWARD_SLASH = "/";
    public static final String PUT_ENDPOINT = "/api/schema-service/v1/schema";
    public static final String PUT_SYSTEM_SCHEMA_ENDPOINT = "/api/schema-service/v1/schemas/system";
    public static final String POST_ENDPOINT = "/api/schema-service/v1/schema";
    public static final String GET_LIST_ENDPOINT = "/api/schema-service/v1/schema";
    public static final String GET_ENDPOINT = "/api/schema-service/v1/schema/";
    public static final String GET_FLATTENED_ENDPOINT = "/api/schema-service/v1/schema/{id}/IndexerSchemaV1";
    public static final String GET_INFO_ENDPOINT = "/api/schema-service/v1/info";
    public static final String GET_LIVENESS_ENDPOINT = "/api/schema-service/v1/liveness_check";
    public static final String GET_SWAGGER_ENDPOINT = "/api/schema-service/v1/swagger";
    public static final String GET_API_DOCS_ENDPOINT = "/api/schema-service/v1/api-docs";
    //public static final String HOST = "http://localhost:8080";
    public static final String INTERNAL_SERVER_ERROR = "internal server error";
    public static final String INTERNAL = "INTERNAL";
    public static final String SCHEMA_KIND = "schema";
    public static final String AUTHORITY = "authority";
    public static final String LATEST_VERSION = "latestVersion";
    public static final String GET_SUCCESSRESPONSECODE = "200";
    public static final String SOURCE = "source";
    public static final String ENTITY_TYPE = "entityType";
    public static final String MAJOR_VERSION = "majorVersion";
    public static final String MINOR_VERSION = "minorVersion";
    public static final String PATCH_VERSION = "patchVersion";
    public static final String CREATED_BY = "createdBy";
    public static final String SUPERSEDED_BY = "supersededBy";
    public static final String DATE_CREATED = "dateCreated";
    public static final String SCHEMA = "schema";
    public static final String OBSOLETE = "OBSOLETE";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String SCOPE = "scope";
    public static final String STATUS = "status";
    public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
    public static final String DATAECOSYSTEM = "dataecosystem";
    public static final String schemaIdOfInputPayload = "schemaInfo.schemaIdentity.id";
    public static final String SCHEMA_IDENTITY = "schemaIdentity";
    public static final String ENTITY = "entityType";
    public static final String SCHEMA_MAJOR_VERSION = "schemaVersionMajor";
    public static final String SCHEMA_MINOR_VERSION = "schemaVersionMinor";
    public static final String SCHEMA_PATCH_VERSION = "schemaVersionPatch";
    public static final String ID = "id";
    public static final String DOT = ".";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String STORAGE_SCHEMA = "StorageSchema";
    public static final String USER = "user";
    public static final String STORAGE_SCHEMA_USER_EMAIL = "testUserEmail@test.com";
    public static final String KIND = "kind";
    public static final String TRUE = "true";
    public static String host() { return EnvLoader.getHost(); }
    public static String dataPartitionId() { return EnvLoader.get("DATA_PARTITION_ID"); }
    public static String sharedTenant() { return EnvLoader.get("SHARED_TENANT"); }
}
