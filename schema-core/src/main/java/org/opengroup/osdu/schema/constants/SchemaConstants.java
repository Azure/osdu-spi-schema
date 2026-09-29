package org.opengroup.osdu.schema.constants;

public class SchemaConstants {

    private SchemaConstants() {

    }

    public static final String NAMESPACE = "dataecosystem";

    // source
    public static final String SOURCE_KIND = "source";

    // entity type
    public static final String ENTITYTYPE_KIND = "entityType";

    // authority
    public static final String AUTHORITY_KIND = "authority";

  //Delimeters
    public static final String SCHEMA_KIND_DELIMITER = ":";
    public static final String SCHEMA_Version_DELIMITER = ".";
    public static final String SCHEMA_KIND_REGEX="^([^.:\\r\\n\\s]+):([^.:\\r\\n\\s]+):([^.:\\r\\n\\s]+):(([\\d]+)[.]([\\d]+)[.]([\\d]+))$";
    public static final String SCHEMA_EMPTY_REGEX = "^[\\w\\-\\.]+$";

    // schema
    public static final String SCHEMA_KIND = "schema";
    public static final String AUTHORITY = "authority";
    public static final String SOURCE = "source";
    public static final String ENTITY_TYPE = "entityType";
    public static final String MAJOR_VERSION = "majorVersion";
    public static final String MINOR_VERSION = "minorVersion";
    public static final String PATCH_VERSION = "patchVersion";
    public static final String CREATED_BY = "createdBy";
    public static final String SUPERSEDED_BY = "supersededBy";
    public static final String DATE_CREATED = "dateCreated";
    public static final String SCHEMA = "schema";
    public static final String SCOPE = "scope";
    public static final String STATUS = "status";
    public static final String TRANSFORMATION_TAG = "x-";
    public static final String TITLE_TAG = "title";

    // general
    public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
    public static final String ACCOUNT_ID = "account-Id";
    //public static final String ACCOUNT_ID_COMMON_PROJECT = "common";
    public static final String ON_BEHALF_OF = "on-Behalf-Of";
    public static final String SERVICE_NAME = "service";


    public static final String ENTITLEMENT_SERVICE_GROUP_VIEWERS = "service.schema-service.viewers";
    public static final String ENTITLEMENT_SERVICE_GROUP_EDITORS = "service.schema-service.editors";
    public static final String WORKFLOW_SYSTEM_ADMIN = "service.schema-service.system-admin";


    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String REF = "$ref";
    public static final String DEFINITIONS = "definitions";
    public static final String AUTHORIZATION = "Authorization";
    public static final String SCHEMA_BUCKET_EXTENSION = "-schema";
    public static final String SCHEMA_GET_STARTED = "Getting schema from  store";
    public static final String SCHEMA_GET_PRIVATE = "Getting schema from private  project";
    public static final String SCHEMA_GET_COMMON = "Getting schema from common  project";
    public static final String SCHEMA_FOUND = "Schema is found in  store";
    public static final String SCHEMA_CREATION_STARTED = "Schema registration started";
    public static final String SCHEMA_INFO_CREATED = "Schema info registered in schema info store";
    public static final String SCHEMA_INFO_UPDATED = "Schema info updated in schema info store";
    public static final String SCHEMA_CREATED = "Schema saved in schema store";
    public static final String AUTHORITY_CREATED = "Authority is created";
    public static final String SOURCE_CREATED = "Source is created";
    public static final String ENTITY_TYPE_CREATED = "EntityType is created";
    public static final String JSON_EXTENSION = ".json";
    public static final String SCHEMA_NOT_FOUND_PRIVATE = "Schema is not present in private tenant";
    public static final String SCHEMA_NOT_FOUND_COMMON = "Schema is not present in common tenant";
    public static final String SCHEMA_UPDATION_STARTED = "Updation of schema with schema id {0} started";
    public static final String SCHEMA_UPDATED = "Schema has been updated";
    public static final String MSG_FORBIDDEN = "User does not have access to the requested resource";

    // audit message
    public static final String AUDIT_LOG_MESSAGE = "Schema service starts";

    // exceptions
    public static final String AUTHORITY_EXISTS_ALREADY_REGISTERED = "Authority already registered";
    public static final String AUTHORITY_EXISTS_EXCEPTION = "Authority already registered with Id: {0}";
    public static final String ENTITY_TYPE_EXISTS_EXCEPTION = "EntityType already registered with Id: {0}";
    public static final String EMPTY_ID = "The id provided is empty";
    public static final String SCHEMA_CREATION_FAILED = "Schema creation failed";
    public static final String SCHEMA_NOTIFICATION_FAILED = "Failed to publish the schema notification.";

    public static final String SYSTEM_SCHEMA_NOTIFICATION_FAILED = "Failed to publish the system schema notification.";
    public static final String SCHEMA_NOTIFICATION_IS_DISABLED = "Schema event notification is turned off.";
    public static final String SCHEMA_UPDATE_FAILED = "Schema updation failed";
    public static final String SCHEMA_UPDATE_EXCEPTION = "Only schema in developement stage can be updated";
    public static final String SCHEMA_PUT_CREATE_EXCEPTION = "Only schema in developement stage can be created through put";
    public static final String INVALID_SCHEMA_UPDATE = "Invalid schema to update, Schema not registered";
    public static final String SCHEMA_CREATION_FAILED_INVALID_OBJECT = "Schema creation failed due to invalid object";
    public static final String INVALID_INPUT = "The input is invalid. Object is invalid";
    public static final String INVALID_SCHEMA_INPUT = "The input is invalid. Schema is not a valid JSON Object";
    public static final String SOURCE_EXISTS_EXCEPTION = "Source already registered with Id: {0}";
    public static final String LATESTVERSION_MINORFILTER_WITHOUT_MAJOR = "MinorVersion Filter is invalid without giving MajorVersion when LatestVersion Schema is required";
    public static final String LATESTVERSION_PATCHFILTER_WITHOUT_MINOR = "PatchVersion Filter is invalid without giving MinorVersion when LatestVersion Schema is required";
    public static final String SCHEMA_CREATE_CLEAN = "Cleaned partial data created by current schema create operation";

    // error message
    public static final String SCHEMA_NOT_PRESENT = "Schema is not present";
    public static final String OBJECT_INVALID = "Object is invalid";
    public static final String SOURCE_EXISTS = "Source already registered";
    public static final String ENTITY_TYPE_EXISTS = "EntityType already registered";
    public static final String INVALID_SUPERSEDEDBY_ID = "Invalid SuperSededBy id";
    public static final String INVALID_SCHEMA_ID = "Invalid Schema id";
    public static final String SCHEMA_UPDATE_ERROR = "Schema is not in development stage, only development stage schema can be updated";
    public static final String SCHEMA_UPDATE_INVALID = "Schema is invalid for updation, it cannot be registered";
    public static final String BAD_INPUT = "Bad input parameter";
    public static final String INVALID_AUTHORIZATION_TOKEN = "Invalid authorization token";
    public static final String SCHEMA_ID_EXISTS = "Schema Id is already present";
    public static final String INVALID_UPDATE_OPERATION = "Update/Create failed because schema id is present in another tenant";
    public static final String UNAUTHORIZED_EXCEPTION = "User is unauthorized to perform this action";
    public static final String BREAKING_CHANGES_PATCH = "Patch version validation failed. Changes requiring a minor or major version increment were found; analysed version: {0} and {1}. Updating the schema version to a higher minor or major version is required.";
    public static final String BREAKING_CHANGES_MINOR = "Minor version validation failed. Breaking changes were found; analysed versions {0} and {1}. Updating the schema version to a higher major version is required.";

    // OSDU
    public static final String DATA_PARTITION_ID = "data-partition-id";
    public static final String CORRELATION_ID = "correlation-id";

    public static final String APPLICATION_NAME = "Schema Service";

    //pub-sub message
    public final static String EVENT_SUBJECT = "schemachanged";
    public final static String SCHEMA_CREATE_EVENT_TYPE = "create";
    public final static String SCHEMA_UPDATE_EVENT_TYPE = "update";
    public final static String KIND = "kind";

    //Swagger
    public final static String GET_SCHEMA_200_RESPONSE = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"description\": \"The entity shapefile.\",\n" +
            "  \"title\": \"ShapeFile\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"definitions\": {},\n" +
            "  \"properties\": {}\n" +
            "}";

  //Schema Validation Constants
    public static enum SkipTags
    {
        TITLE("title"),
        DESCRIPTION("description"),
        EXAMPLES("examples"),
        EXAMPLE("example"),
        PATTERN("pattern"),
        ID("$id"),
        COMMENT("$comment");
        private final String value;
        private SkipTags(String value)
        {
            this.value = value;
        }
        public String getValue()
        {
            return this.value;
        }
    }


    //schema-patch operation
    public final static String OP_ADD = "add";
    public final static String OP_REMOVE = "remove";
    public final static String OP_REPLACE = "replace";

    //Schema Composition Tags
    public static enum CompositionTags
    {
        ALL_OF("allOf"),
        ONE_OF("oneOf"),
        ANY_OF("anyOf");

        private final String value;

        private CompositionTags(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return this.value;
        }
    }

    public static final String ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS = "service.system-schema-service.editors";
    public static final String SYSTEM_PARTITION_NAME = "system";
}
