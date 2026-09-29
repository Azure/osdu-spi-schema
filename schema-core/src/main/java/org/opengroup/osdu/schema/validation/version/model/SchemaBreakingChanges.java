package org.opengroup.osdu.schema.validation.version.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@RequiredArgsConstructor
@ToString
public class SchemaBreakingChanges {

	private final SchemaPatch path;
	private final String message;
}
