package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.AuthUtil;
import org.opengroup.osdu.schema.util.HttpClientFactory;
import org.opengroup.osdu.schema.util.JsonUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class SchemaServiceStepDef_PUT {

	@Inject
	private SchemaServiceScope context;

	static String[] GetListBaseFilterArray;
	static String[] GetListVersionFilterArray;
	String queryParameter;

	List<HashMap<String, String>> list_schemaParameterMap = new ArrayList<HashMap<String, String>>();

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} and mark schema as {string}")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_and_mark_schema_as_string(String inputPayload, String tenant, String status) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + currentPatchVersion;
		updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().remove("status");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().addProperty("status",
	status);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} and mark schema as {string} for next major version")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_and_mark_schema_as_string_for_next_major_version(String inputPayload, String tenant, String status) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		currentMajorVersion = currentMajorVersion + 1;
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + ".0";
		updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().remove("status");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().addProperty("status",
	status);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string}")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + currentPatchVersion;
		updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} for superceded input")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_for_superceded_input(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String newSchemaStr = this.context.getFileUtils().read(inputPayload);
		JsonElement newSchemaJsonBody = new Gson().fromJson(newSchemaStr, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int patchMajorVersion = Integer.parseInt(this.context.getSchemaVersionPatch());

		String latestSchemaResp = this.context.getHttpResponse().getBody();
		JsonElement latestSchemaRespJsonBody = new Gson().fromJson(latestSchemaResp, JsonElement.class);

		JsonElement supersededByBody = new Gson().fromJson(
	latestSchemaRespJsonBody.getAsJsonObject().getAsJsonArray("schemaInfos").get(0)
.getAsJsonObject().getAsJsonObject(TestConstants.SCHEMA_IDENTITY).toString(),
	JsonElement.class);

		String newID = "SchemaSanityTest:testSource:testEntity:" + (currentMajorVersion + 1) + "."
	+ currentMinorVersion + "." + patchMajorVersion;
		String supersededById = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + patchMajorVersion;
		updateVersionInJsonBody(newSchemaJsonBody, currentMinorVersion, currentMajorVersion + 1, newID);

		newSchemaStr = new Gson().toJson(newSchemaJsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);

		// Create new Schema
		HttpRequest httpPostRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.POST_ENDPOINT).body(newSchemaStr)
	.httpMethod(HttpRequest.POST).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse postResponse = HttpClientFactory.getInstance().send(httpPostRequest);

		assertEquals(201, postResponse.getCode());

		// Update with superceded by ID
		String postSchemaBody = postResponse.getBody();
		JsonElement postSchemaJsonBody = new Gson().fromJson(postSchemaBody, JsonElement.class);

		postSchemaJsonBody.getAsJsonObject().add(TestConstants.SUPERSEDED_BY, supersededByBody);

		JsonObject putRequest = new JsonObject();
		putRequest.add("schemaInfo", postSchemaJsonBody);
		putRequest.add("schema", new Gson().fromJson("{}", JsonElement.class));
		this.context.setSchemaIdFromInputPayload(newID);
		this.context.setSupersededById(supersededById);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(putRequest.toString()).httpMethod(HttpRequest.PUT)
	.requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);

		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased patch version only")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_patch_version_only(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int nextPatchVersion = currentPatchVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + nextPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, nextPatchVersion,
	schemaId);
		HttpResponse response = putRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string} and data-partition-id as {string} with increased minor version with 2 count")
	public void i_hit_schema_service_put_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_with_2_count(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int nextMinorVersion = currentMinorVersion + 2;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ nextMinorVersion + "." + currentPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion,
	schemaId);
		HttpResponse response = putRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
		prepareSchemaParameterMapList();
	}

	@Given("I hit schema service PUT API with {string} and data-partition-id as {string} with less minor version by 1 count than earlier")
	public void i_hit_schema_service_put_api_with_string_and_datapartitionid_as_string_with_less_minor_version_by_1_count_than_earlier(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int previousMinorVersion = currentMinorVersion - 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ previousMinorVersion + ".0";
		this.context.setSchemaIdFromInputPayload(schemaId);
		updateVersionInJsonBody(jsonBody, previousMinorVersion, currentMajorVersion,
	schemaId);
		HttpResponse response = putRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased minor version and patch version")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_minor_version_and_patch_version(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int nextMinorVersion = currentMinorVersion + 1;
		int nextPatchVersion = currentPatchVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ nextMinorVersion + "." + nextPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, nextPatchVersion,
	schemaId);
		HttpResponse response = putRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased minor version")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_minor_version(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int nextMinorVersion = currentMinorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ nextMinorVersion + "." + currentPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updateVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion,
	schemaId);
		HttpResponse response = putRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased minor version only")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_minor_version_only(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		currentMinorVersion = currentMinorVersion + 1;
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + currentPatchVersion;
		updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} with different entityType")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_different_entitytype(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion +  "." + currentPatchVersion;
		updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		int randomNum = (int) (Math.random() * 10000);
		String entityVal = "testEntity" + randomNum;
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
	.remove("entityType");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
	.addProperty("entityType", entityVal);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service PUT API with {string}, data-partition-id as {string} with next major version")
	public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_next_major_version(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		currentMajorVersion = currentMajorVersion + 1;
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + currentPatchVersion;
		updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("put schema service should respond back with {string}")
	public void put_schema_service_should_respond_back_with_string(String ReponseStatusCode) {
		HttpResponse response = this.context.getHttpResponse();
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
	}

	@Then("the put service for supersededBy should respond back with {string}")
	public void the_put_service_for_supersededby_should_respond_back_with_string(String ReponseStatusCode) {
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
			Assert.assertNotNull(getResponseValue(TestConstants.SUPERSEDED_BY));
			assertEquals(getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.ID),
		this.context.getSchemaIdFromInputPayload());
			assertEquals(getResponseValue(TestConstants.SUPERSEDED_BY + TestConstants.DOT + TestConstants.ID),
		this.context.getSupersededById());
		}
	}

	@Given("I hit schema service PUT API for supersededBy with {string} and data-partition-id as {string}")
	public void i_hit_schema_service_put_api_for_supersededby_with_string_and_datapartitionid_as_string(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);

		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String supersededById = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + currentPatchVersion;
		updateSupersededByInJsonBody(jsonBody, supersededById, currentMajorVersion, currentMinorVersion,
	currentPatchVersion);
		this.context.setSupersededById(supersededById);
		int nextMinorVersion = currentMinorVersion + 1;
		int nextMajorVersion = currentMajorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
	+ nextMinorVersion + ".0";
		this.context.setSchemaIdFromInputPayload(schemaId);
		updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, schemaId);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	private void updateVersionInJsonBody(JsonElement jsonBody, int nextMinorVersion, int nextMajorVersion, String id) {
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.remove("schemaVersionMinor");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.addProperty("schemaVersionMinor", nextMinorVersion);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.remove("schemaVersionMajor");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.addProperty("schemaVersionMajor", nextMajorVersion);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject().remove("id");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.addProperty("id", id);
	}
	
	private void updatePatchVersionInJsonBody(JsonElement jsonBody, int nextMinorVersion, int nextMajorVersion,
			int nextPatchVersion, String id) {
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.remove("schemaVersionMinor");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.addProperty("schemaVersionMinor", nextMinorVersion);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.remove("schemaVersionMajor");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.addProperty("schemaVersionMajor", nextMajorVersion);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.remove("schemaVersionPatch");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.addProperty("schemaVersionPatch", nextPatchVersion);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject().remove("id");
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
				.addProperty("id", id);
	}
	
	private HttpResponse putRequest(JsonElement jsonBody, String schemaId, String tenant) {
		this.context.setSchemaIdFromInputPayload(schemaId);
		this.context.setSchemaFromInputPayload(jsonBody.getAsJsonObject().get(TestConstants.SCHEMA).toString());
		this.context.setJsonPayloadForPostPUT(jsonBody.toString());
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		this.context.setAuthHeaders(headers);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_ENDPOINT)
				.body(jsonBody.toString()).httpMethod(HttpRequest.PUT).requestHeaders(headers).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		return response;
	}


	public void prepareSchemaParameterMapList() {
		String response = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		JsonObject root = gsn.fromJson(response, JsonObject.class);
		JsonObject schemaIdentity_ForEachSchemaInfo = (JsonObject) root.get("schemaIdentity");
		this.context.setSchemaVersionMajor(schemaIdentity_ForEachSchemaInfo.get("schemaVersionMajor").getAsString());
		this.context.setSchemaVersionMinor(schemaIdentity_ForEachSchemaInfo.get("schemaVersionMinor").getAsString());
	}

	private void commonAssertion(HttpResponse response, JsonObject jsonBody) {

		assertEquals(getExpectedValue(jsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.AUTHORITY),
				getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.AUTHORITY));

		assertEquals(getExpectedValue(jsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.SOURCE),
				getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.SOURCE));

		assertEquals(getExpectedValue(jsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.ENTITY),
				getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.ENTITY));

		Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
		Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
	}

	private String getExpectedValue(JsonObject jsonBody, String parentAttributeValue, String valueToBeRetrieved) {
		String value;
		if (parentAttributeValue == null) {
			value = jsonBody.get(valueToBeRetrieved).toString();
			return value.substring(1, value.length() - 1);
		} else {
			value = jsonBody.getAsJsonObject(parentAttributeValue).get(valueToBeRetrieved).toString();
			if (Character.isDigit(value.charAt(0))) {
				return value;
			} else {
				return value.substring(1, value.length() - 1);
			}
		}
	}

	private String getResponseValue(String responseAttribute) {
		return JsonUtils.getAsJsonPath(this.context.getHttpResponse().getBody().toString()).get(responseAttribute)
				.toString();
	}

	private void updateSupersededByInJsonBody(JsonElement jsonBody, String id, int majorVersion, int minorVersion,
			int patchVersion) {

		JsonElement supersededByBody = new Gson().fromJson(jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
				.getAsJsonObject(TestConstants.SCHEMA_IDENTITY).toString(), JsonElement.class);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").add(TestConstants.SUPERSEDED_BY, supersededByBody);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.remove(TestConstants.ID);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.addProperty(TestConstants.ID, id);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.remove(TestConstants.SCHEMA_MAJOR_VERSION);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.addProperty(TestConstants.SCHEMA_MAJOR_VERSION, majorVersion);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.remove(TestConstants.SCHEMA_MINOR_VERSION);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.addProperty(TestConstants.SCHEMA_MINOR_VERSION, minorVersion);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.remove(TestConstants.SCHEMA_PATCH_VERSION);
		jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
				.addProperty(TestConstants.SCHEMA_PATCH_VERSION, patchVersion);
	}

	private String selectTenant(String tenant) {

		switch (tenant) {
		case "TENANT1":
			tenant = TestConstants.PRIVATE_TENANT1;
			break;
		case "TENANT2":
			tenant = TestConstants.PRIVATE_TENANT2;
			break;
		case "COMMON":
			tenant = TestConstants.SHARED_TENANT;
			break;
		default:
			System.out.println("Invalid tenant");
		}
		return tenant;
	}
}