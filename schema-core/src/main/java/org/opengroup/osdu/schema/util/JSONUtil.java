package org.opengroup.osdu.schema.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonPatch;
import jakarta.json.JsonPointer;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.constants.SchemaConstants.CompositionTags;
import org.opengroup.osdu.schema.constants.SchemaConstants.SkipTags;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JSONUtil {

	private EnumSet<SkipTags> skipTags = EnumSet.allOf(SkipTags.class);
	private EnumSet<CompositionTags> compositionTags = EnumSet.allOf(CompositionTags.class);

	public JsonNode getCleanJSON(String inputJSON) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(inputJSON);
		clean(root, "");

		return root;
	}

	public Optional<JsonNode> removeElement(JsonNode element, String filedName){

		Optional removedElement = Optional.empty();
		if(element != null && element.has(filedName)) {
			removedElement = Optional.of(((ObjectNode)element).remove(filedName));
		}
		return removedElement;
	}

	private boolean checkIfParentIsCompositionTag(String path) {
		String [] tags = path.split("/");
		if(tags.length < 3)
			return false;
		boolean isCompositionTag = compositionTags.stream().anyMatch(tag -> tag.getValue().equals(tags[tags.length-2]));
		return isCompositionTag;
	}

	public  List<SchemaPatch> findJSONDiff(JsonNode source, JsonNode target) throws JsonMappingException, JsonProcessingException {
		JsonValue sourceValue = Json.createReader(new StringReader(source.toString())).readValue();
		JsonValue targetValue = Json.createReader(new StringReader(target.toString())).readValue();
		JsonPatch diff = Json.createDiff(sourceValue.asJsonObject(), targetValue.asJsonObject());
		return new ObjectMapper().readValue(diff.toString(), TypeFactory.defaultInstance().constructCollectionType(List.class,  
				SchemaPatch.class));

	}

	public  JsonArray getJsonArrayFromGivenPath(String jsonString, String rootPath) {
		try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
			JsonStructure jsonStruct = reader.read();
			JsonPointer jp = Json.createPointer(rootPath); 
			JsonValue oldJsonVal = jp.getValue(jsonStruct);
			return oldJsonVal.asJsonArray();
		}
	}

	private void clean(JsonNode node, String path) {
		if (node.isObject()) {
			ObjectNode object = (ObjectNode) node;
			Iterator<Entry<String, JsonNode>> itr = object.fields();
			while(itr.hasNext()) {

				Entry<String, JsonNode> entry = itr.next();
				String key = entry.getKey();
				if(skipTags.stream().anyMatch(tag -> tag.getValue().equals(key))
						|| entry.getKey().startsWith(SchemaConstants.TRANSFORMATION_TAG)) {
					//Don't delete the title tag of elements inside composition tag
					//This will be utilized later for comparision
					if(SchemaConstants.TITLE_TAG.equals(key)
							&& checkIfParentIsCompositionTag(path)){
						continue;
					}
					itr.remove();
				}
				clean(entry.getValue(), path + "/" + entry.getKey());
			}
		} else if (node.isArray()) {
			ArrayNode array = (ArrayNode) node;
			Iterator<JsonNode> itr = array.elements();
			AtomicInteger counter = new AtomicInteger();
			while(itr.hasNext()) {
				JsonNode entry = itr.next();
				if(skipTags.stream().anyMatch(tag -> tag.getValue().equals(entry))
						|| entry.asText().startsWith(SchemaConstants.TRANSFORMATION_TAG)) {
					itr.remove();
				}
				clean(entry, path + "/" + counter.getAndIncrement());
			}
		}
	}

	class JSONCleaner {

		private final Map<String, String> json = new LinkedHashMap<>(64);
		private final JsonNode root;
		JSONCleaner(JsonNode node) {
			this.root = Objects.requireNonNull(node);
		}

		public Map<String, String> cleanAndGetRef() {
			process(root, "");
			return json;
		}

		private void process(JsonNode node, String path) {
			if (node.isObject()) {
				ObjectNode object = (ObjectNode) node;
				Iterator<Entry<String, JsonNode>> itr = object.fields();
				while(itr.hasNext()) {

					Entry<String, JsonNode> entry = itr.next();
					String key = entry.getKey();
					if(skipTags.stream().anyMatch(tag -> tag.getValue().equals(key))
							|| entry.getKey().startsWith(SchemaConstants.TRANSFORMATION_TAG)) {
						//Don't delete the title tag of elements inside composition tag
						//This will be utilized later for comparision
						if(SchemaConstants.TITLE_TAG.equals(key)
								&& checkIfParentIsCompositionTag(path)){
							continue;
						}
						itr.remove();
					}
					process(entry.getValue(), path + "/" + entry.getKey());
				}
			} else if (node.isArray()) {
				ArrayNode array = (ArrayNode) node;
				Iterator<JsonNode> itr = array.elements();
				AtomicInteger counter = new AtomicInteger();
				while(itr.hasNext()) {
					JsonNode entry = itr.next();
					if(skipTags.stream().anyMatch(tag -> tag.getValue().equals(entry))
							|| entry.asText().startsWith(SchemaConstants.TRANSFORMATION_TAG)) {
						itr.remove();
					}
					process(entry, path + "/" + counter.getAndIncrement());
				}
			}else {

				//Check if $ref has value in the form of 
				Pattern pattern = Pattern.compile(SchemaConstants.SCHEMA_KIND_REGEX, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(node.toString());

				if(path.endsWith("$ref")) {
					System.out.println(node.toString()+" "+matcher.find());
					json.put(path, node.toString());
				}
			}
		}


	}
}
