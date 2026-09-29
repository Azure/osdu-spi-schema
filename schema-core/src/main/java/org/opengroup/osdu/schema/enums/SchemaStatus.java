package org.opengroup.osdu.schema.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "SchemaStatus", description = "Schema lifecycle status", example = "PUBLISHED")
public enum SchemaStatus {

    PUBLISHED, OBSOLETE, DEVELOPMENT

}
