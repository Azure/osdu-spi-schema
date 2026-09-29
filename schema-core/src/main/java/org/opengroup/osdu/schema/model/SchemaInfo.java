package org.opengroup.osdu.schema.model;

import java.util.Date;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Schema Info", description = "Represents a model to Schema Info including status, creation and schemaIdentity")
public class SchemaInfo {

	@NotNull(message = "schemaIdentity must not be null")
	@Valid
	@Schema(description = "Schema identity information including authority, source, entity type and version")
	private SchemaIdentity schemaIdentity;

	@Schema(description = "The user who created the schema. This value is taken from API caller token.", example = "user@opendes.com")
	private String createdBy;

	@Schema(description = "The UTC date time of the entity creation", example = "2019-05-23T11:16:03Z")
	private Date dateCreated;

	@NotNull(message = "status must not be null")
	@Schema(description = "Schema lifecycle status", example = "PUBLISHED")
	private SchemaStatus status;

	@Schema(description = "Schema visibility — INTERNAL (partition-scoped) or SHARED (platform-wide). System-assigned based on the partition-id.", example = "INTERNAL")
	private SchemaScope scope;

	@JsonInclude(Include.NON_NULL)
	@Valid
	@Schema(description = "The schema identity of the schema that supersedes this one. Present only when this schema has been superseded.")
	private SchemaIdentity supersededBy;

}