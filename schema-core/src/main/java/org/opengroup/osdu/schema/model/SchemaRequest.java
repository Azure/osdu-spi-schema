package org.opengroup.osdu.schema.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.schema.validation.request.SchemaRequestConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Represents a model to Schema Request")
public class SchemaRequest {

	@NotNull(message = "schemaInfo must not be null")
	@Valid
	@Schema(description = "Schema identity and metadata")
	private SchemaInfo schemaInfo;

	@NotNull(message = "schema must not be null")
	@SchemaRequestConstraint
	@Schema(description = "The JSON Schema definition as a JSON object", type = "object", example = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Wellbore\",\"type\":\"object\"}")
	private Object schema;

}
