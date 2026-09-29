package org.opengroup.osdu.schema.validation.version.model;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class SchemaPatch {
	
	private String op;
	private String path;
	private JsonNode value;
}
