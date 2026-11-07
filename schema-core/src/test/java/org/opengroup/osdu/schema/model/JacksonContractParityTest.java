package org.opengroup.osdu.schema.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;
import org.opengroup.osdu.schema.errors.Error;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import tools.jackson.databind.json.JsonMapper;

/**
 * The schema API was served by the Jackson 2 mapper from os-core-common 9.x and is now served by the Jackson 3
 * mapper from os-core-common 10. Writes and reads the service's own API models with both and compares the output,
 * so any change to the wire format fails here. Written JSON is compared as text, property order included: Jackson 3
 * sorts properties alphabetically, and clients (the acceptance tests among them) compare error bodies as strings.
 * SchemaUpsertResponse is left out on purpose: it never reaches the wire (controllers return its SchemaInfo and use
 * httpCode as the status), and Jackson 3 would write its HttpStatus with toString(), e.g. "201 CREATED".
 */
public class JacksonContractParityTest {

	private com.fasterxml.jackson.databind.ObjectMapper jackson2;
	private JsonMapper jackson3;

	// Both mappers write dates in the JVM default time zone; services run in UTC, where Jackson 3 would write "Z".
	private void useTimeZone(String zone) {
		TimeZone timeZone = TimeZone.getTimeZone(zone);
		jackson2 = new com.fasterxml.jackson.databind.ObjectMapper()
				.registerModule(new JavaTimeModule())
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.setTimeZone(timeZone);
		jackson3 = new HttpConfiguration().jsonObjectMapper().rebuild().defaultTimeZone(timeZone).build();
	}

	@ParameterizedTest
	@ValueSource(strings = {"UTC", "Europe/Oslo"})
	public void responseModelsHaveTheSameWireFormat(String zone) throws Exception {
		useTimeZone(zone);
		SchemaInfo published = schemaInfo(new Date(1558610163123L), identity("osdu", "wks", "wellbore", 1L, 1L, 0L));
		published.setSupersededBy(identity("osdu", "wks", "wellbore", 1L, 2L, 0L));
		SchemaInfo sparse = new SchemaInfo();
		sparse.setSchemaIdentity(new SchemaIdentity());

		assertSameJson(published);
		assertSameJson(sparse);
		assertSameJson(SchemaInfoResponse.builder().schemaInfos(List.of(published, sparse)).offset(0).count(2)
				.totalCount(7).build());
		assertSameJson(SchemaInfoResponse.builder().build());
		assertSameJson(new SchemaRequest(published, schemaBody()));

		Error error = new Error(HttpStatus.BAD_REQUEST);
		error.setCode(400);
		error.setMessage("Validation Error");
		error.addError("schema must not be null");
		assertSameJson(error);
	}

	@ParameterizedTest
	@ValueSource(strings = {"UTC", "Europe/Oslo"})
	public void requestModelsAreReadTheSame(String zone) throws Exception {
		useTimeZone(zone);
		assertSameRead("""
				{"schemaInfo":{"schemaIdentity":{"authority":"osdu","source":"wks","entityType":"wellbore",
				"schemaVersionMajor":1,"schemaVersionMinor":0,"schemaVersionPatch":0,"unknown":"x"},
				"status":"DEVELOPMENT","dateCreated":"2019-05-23T11:16:03Z"},
				"schema":{"title":"Wellbore","type":"object","properties":{"a":{"type":"number","default":1.50}},
				"required":["a"],"x-osdu-tag":null},"extra":1}
				""", SchemaRequest.class);
		assertSameRead("""
				{"schemaInfo":{"schemaIdentity":{"authority":"osdu","source":"wks","entityType":"wellbore",
				"schemaVersionMajor":"1","schemaVersionMinor":0,"schemaVersionPatch":0},
				"status":"PUBLISHED","scope":"INTERNAL","dateCreated":"2019-05-23T11:16:03.123+00:00"},"schema":{}}
				""", SchemaRequest.class);
		assertSameRead("""
				{"schemaIdentity":{"authority":"osdu","schemaVersionMajor":null},"dateCreated":1558610163123,"status":"OBSOLETE"}
				""", SchemaInfo.class);
	}

	private void assertSameJson(Object value) throws Exception {
		String expected = jackson2.writeValueAsString(value);
		assertEquals(expected, jackson3.writeValueAsString(value));
	}

	private void assertSameRead(String json, Class<?> type) throws Exception {
		String expected = jackson2.writeValueAsString(jackson2.readValue(json, type));
		String actual = jackson2.writeValueAsString(jackson3.readValue(json, type));
		assertEquals(jackson2.readTree(expected), jackson2.readTree(actual), expected + " <> " + actual);
	}

	private static SchemaInfo schemaInfo(Date created, SchemaIdentity identity) {
		return SchemaInfo.builder().schemaIdentity(identity).createdBy("user@opendes.com").dateCreated(created)
				.status(SchemaStatus.PUBLISHED).scope(SchemaScope.SHARED).build();
	}

	private static SchemaIdentity identity(String authority, String source, String entityType, Long major, Long minor,
			Long patch) {
		return SchemaIdentity.builder().authority(authority).source(source).entityType(entityType)
				.schemaVersionMajor(major).schemaVersionMinor(minor).schemaVersionPatch(patch)
				.id(authority + ":" + source + ":" + entityType + ":" + major + "." + minor + "." + patch).build();
	}

	private static Map<String, Object> schemaBody() {
		Map<String, Object> property = new LinkedHashMap<>();
		property.put("type", "string");
		property.put("description", null);
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("title", "Wellbore");
		schema.put("properties", Map.of("name", property));
		schema.put("required", List.of("name"));
		schema.put("x-osdu-inheriting-from-kind", null);
		return schema;
	}
}
