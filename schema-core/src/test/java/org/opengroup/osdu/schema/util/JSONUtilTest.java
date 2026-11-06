package org.opengroup.osdu.schema.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opengroup.osdu.schema.constants.SchemaConstants.CompositionTags;
import org.opengroup.osdu.schema.constants.SchemaConstants.SkipTags;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JSONUtilTest {
	
	@InjectMocks
	JSONUtil jsonUtil;
	
	private EnumSet<CompositionTags> compositionTags = EnumSet.allOf(CompositionTags.class);

	@Test
	public void testGetCleanJSON() throws IOException {
		String testJSON = new FileUtils().read("/schema_compare/schema-with-all-permissible-changes.json");
		JsonNode cleanJSON = jsonUtil.getCleanJSON(testJSON);
		assertTrue(verifyCleanJSON(cleanJSON));
	}
	
	public boolean verifyCleanJSON(JsonNode inputJSON) throws DatabindException, JacksonException {
		EnumSet<SkipTags> skipTags = EnumSet.allOf(SkipTags.class);
		Map<String, JsonNode> flattenedJSON = new JSONFlattener(inputJSON).flatten();
		
		for(String key : flattenedJSON.keySet()) {
			System.out.println("key "+key);
			boolean isTitleTag =StringUtils.substringAfterLast(key, "/").equals("title");
			if(isTitleTag) {
				String[] tags = key.split("/");
				boolean compositionTagContainsTitleTag = compositionTags.stream().anyMatch(tag -> tag.getValue().equals(tags[tags.length-3]));
				if(compositionTagContainsTitleTag)
					continue;
			}
			
			if(skipTags.stream().anyMatch(element -> 
			
			element.getValue().equals(StringUtils.substringAfterLast(key, "/")))) {
				return false;
			}
		}
		
		return true;
	}
	
	class JSONFlattener {

		private final Map<String, JsonNode> json = new LinkedHashMap<>(64);
		private final JsonNode root;
		JSONFlattener(JsonNode node) {
			this.root = Objects.requireNonNull(node);
		}

		public Map<String, JsonNode> flatten() {
			process(root, "");
			return json;
		}
		
		private void process(JsonNode node, String prefix) {
			if (node.isObject()) {
				ObjectNode object = (ObjectNode) node;
				object
				.properties().iterator()
				.forEachRemaining(
						entry -> {
							process(entry.getValue(), prefix + "/" + entry.getKey());
						});
			} else if (node.isArray()) {
				ArrayNode array = (ArrayNode) node;
				AtomicInteger counter = new AtomicInteger();
				array
				.values().iterator()
				.forEachRemaining(
						item -> {
							process(item, prefix + "/" + counter.getAndIncrement());
						});
			} else {
				json.put(prefix, node);
			}
		}
		
		
	}

}
