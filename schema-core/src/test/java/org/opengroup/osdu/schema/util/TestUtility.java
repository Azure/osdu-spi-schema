package org.opengroup.osdu.schema.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonPatch;
import jakarta.json.JsonValue;

import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

public class TestUtility {
	
	public static JsonNode getJsonNodeFromFile(String filePath) throws IOException {
		String schema = new FileUtils().read(filePath);
		return new ObjectMapper().readTree(schema);
	}
	
	public static List<SchemaPatch> findSchemaPatch(JsonNode source, JsonNode target) throws DatabindException, JacksonException {
		JsonValue sourceValue = Json.createReader(new StringReader(source.toString())).readValue();
		JsonValue targetValue = Json.createReader(new StringReader(target.toString())).readValue();
		JsonPatch diff = Json.createDiff(sourceValue.asJsonObject(), targetValue.asJsonObject());
		return new ObjectMapper().readValue(diff.toString(), TypeFactory.createDefaultInstance().constructCollectionType(List.class,  
				SchemaPatch.class));

	}
	
}
