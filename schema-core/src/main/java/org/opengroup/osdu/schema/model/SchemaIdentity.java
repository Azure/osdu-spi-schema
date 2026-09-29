package org.opengroup.osdu.schema.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengroup.osdu.schema.constants.SchemaConstants;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SchemaIdentity", description = "Schema authority source and type description")
public class SchemaIdentity {

    @Schema(description = "Entity authority", example = "osdu")
    @NotNull(message = "authority must not be null")
    @Pattern(regexp = SchemaConstants.SCHEMA_EMPTY_REGEX, message = "authority must not contain whitespaces and special characters except - and .")
    private String authority;

    @Schema(description = "Entity source", example = "wks")
    @NotNull(message = "source must not be null")
    @Pattern(regexp = SchemaConstants.SCHEMA_EMPTY_REGEX, message = "source must not contain whitespaces and special characters except - and .")
    private String source;

    @Schema(description = "EntityType Code", example = "wellbore")
    @NotNull(message = "entityType must not be null")
    @Pattern(regexp = SchemaConstants.SCHEMA_EMPTY_REGEX, message = "entityType must not contain whitespaces and special characters except - and .")
    private String entityType;

    @Schema(description = "Major Schema Version Number", example = "1")
    @NotNull(message = "schemaVersionMajor must not be null")
    private Long schemaVersionMajor;

    @Schema(description = "Minor Schema Version Number", example = "1")
    @NotNull(message = "schemaVersionMinor must not be null")
    private Long schemaVersionMinor;

    @Schema(description = "Patch Schema Version Number", example = "0")
    @NotNull(message = "schemaVersionPatch must not be null")
    private Long schemaVersionPatch;

    @Schema(description = "A read-only system defined id used for referencing of a schema.", example = "osdu:wks:wellbore:1.0.0")
    private String id;

}
