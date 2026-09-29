package org.opengroup.osdu.schema.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonPatch;
import jakarta.json.JsonValue;

import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TestUtility {
	
	public static JsonNode getJsonNodeFromFile(String filePath) throws IOException {
		String schema = new FileUtils().read(filePath);
		return new ObjectMapper().readTree(schema);
	}
	
	public static List<SchemaPatch> findSchemaPatch(JsonNode source, JsonNode target) throws JsonMappingException, JsonProcessingException {
		JsonValue sourceValue = Json.createReader(new StringReader(source.toString())).readValue();
		JsonValue targetValue = Json.createReader(new StringReader(target.toString())).readValue();
		JsonPatch diff = Json.createDiff(sourceValue.asJsonObject(), targetValue.asJsonObject());
		return new ObjectMapper().readValue(diff.toString(), TypeFactory.defaultInstance().constructCollectionType(List.class,  
				SchemaPatch.class));

	}
	
}
