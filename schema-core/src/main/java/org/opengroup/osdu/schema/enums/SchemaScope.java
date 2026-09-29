package org.opengroup.osdu.schema.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Schema Scope", description = " Schema Scope - is it internal or shared. This is a system defined attribute based on partition-id passed.", example = "INTERNAL")
public enum SchemaScope {

    INTERNAL, SHARED

}
