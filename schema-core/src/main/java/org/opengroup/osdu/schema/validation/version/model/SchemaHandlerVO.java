package org.opengroup.osdu.schema.validation.version.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SchemaHandlerVO {
	
	
	public SchemaHandlerVO(JsonNode sourceSchema, JsonNode targetSchema, JsonNode sourceDefinition, JsonNode targetDefinition, SchemaValidationType validationType) {
		
		this.sourceSchema = sourceSchema;
		this.targetSchema = targetSchema;
		this.sourceDefinition = sourceDefinition;
		this.targetDefinition = targetDefinition;
		this.validationType = validationType;
		this.changedRefIds = new HashMap<>();
		this.processedArrayPath = new HashSet<>();
	}
	
	private JsonNode sourceSchema;
	private JsonNode targetSchema;
	private JsonNode sourceDefinition;
	private JsonNode targetDefinition;
	private  Map<String, String> changedRefIds;
	private Set<String> processedArrayPath;
	private SchemaValidationType validationType;
	
}
