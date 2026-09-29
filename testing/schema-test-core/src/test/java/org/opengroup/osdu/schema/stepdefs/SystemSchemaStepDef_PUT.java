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

public class SystemSchemaStepDef_PUT {

    @Inject
    private SchemaServiceScope context;

    static String[] GetListBaseFilterArray;
    static String[] GetListVersionFilterArray;
    String queryParameter;
	
    List<HashMap<String, String>> list_schemaParameterMap = new ArrayList<HashMap<String, String>>();

	@Given("I generate user token and set request headers for system API")
	public void i_generate_user_token_and_set_request_headers_for_system_api() throws Exception {
		if (this.context.getToken() == null) {
			String token = new AuthUtil().getToken();
			this.context.setToken(token);
		}

		if (this.context.getAuthHeaders() == null) {
			Map<String, String> authHeaders = new HashMap<String, String>();
			authHeaders.put(TestConstants.AUTHORIZATION, this.context.getToken());
			authHeaders.put(TestConstants.CONTENT_TYPE, TestConstants.JSON_CONTENT);
			authHeaders.remove(TestConstants.DATA_PARTITION_ID);
			this.context.setAuthHeaders(authHeaders);
		} else {
			Map<String, String> authHeaders = this.context.getAuthHeaders();
			authHeaders.remove(TestConstants.DATA_PARTITION_ID);
			this.context.setAuthHeaders(authHeaders);
		}
	}

	@Given("I hit system schema PUT API with {string} only if status is not development")
	public void i_hit_system_schema_put_api_with_string_only_if_status_is_not_development(String inputPayload) throws Exception {
		String resp = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		JsonObject schemaInfosList = gsn.fromJson(resp, JsonObject.class);
		JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
		if (!"DEVELOPMENT".equals(context.getStatus()) || (root.size() == 0) || "INTERNAL".equals(context.getScope())) {
			String body = this.context.getFileUtils().read(inputPayload);
			JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
			int currentMinorVersion = 0;
			int currentMajorVersion = 0;
			int currentPatchVersion = 0;
			if (root.size() > 0) {
				currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
				currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
				currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
			}

			int nextMinorVersion = currentMinorVersion + 1;
			int nextMajorVersion = currentMajorVersion + 1;
			String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
		+ nextMinorVersion + "." + currentPatchVersion;

			this.context.setSchemaIdFromInputPayload(schemaId);

			updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, currentPatchVersion, schemaId);
			Map<String, String> headers = this.context.getAuthHeaders();
			body = new Gson().toJson(jsonBody);
			HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
		.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
		.build();
			HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);

			assertEquals("201", String.valueOf(response.getCode()));
			this.context.setHttpResponse(response);
			prepareSchemaParameterMapList();
		}
	}

	@Given("I hit system schema PUT API with {string} and mark schema as {string}.")
	public void i_hit_system_schema_put_api_with_string_and_mark_schema_as_string(String inputPayload, String status) throws Exception {
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
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit system schema PUT API with {string} and mark schema as {string} for next major version")
	public void i_hit_system_schema_put_api_with_string_and_mark_schema_as_string_for_next_major_version(String inputPayload, String status) throws Exception {
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
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit system schema PUT API with {string}")
	public void i_hit_system_schema_put_api_with_string(String inputPayload) throws Exception {
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
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit system schema PUT API with {string} for superceded input")
	public void i_hit_system_schema_put_api_with_string_for_superceded_input(String inputPayload) throws Exception {
		String newSchemaStr = this.context.getFileUtils().read(inputPayload);
		JsonElement newSchemaJsonBody = new Gson().fromJson(newSchemaStr, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int patchMajorVersion = Integer.parseInt(this.context.getSchemaVersionPatch());

		String latestSchemaResp = this.context.getHttpResponse().getBody();
		JsonElement latestSchemaRespJsonBody = new Gson().fromJson(latestSchemaResp, JsonElement.class);

		JsonElement supersededByBody = new Gson().fromJson(latestSchemaRespJsonBody.getAsJsonObject().getAsJsonArray("schemaInfos")
	.get(0).getAsJsonObject().getAsJsonObject(TestConstants.SCHEMA_IDENTITY).toString(), JsonElement.class);

		String newID = "SchemaSanityTest:testSource:testEntity:" + (currentMajorVersion + 1) + "."
	+ currentMinorVersion + "." + patchMajorVersion;
		String supersededById = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + patchMajorVersion;
		updateVersionInJsonBody(newSchemaJsonBody, currentMinorVersion, currentMajorVersion + 1, newID);

		newSchemaStr = new Gson().toJson(newSchemaJsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.remove(TestConstants.DATA_PARTITION_ID);

		//Create new Schema
		HttpRequest httpPostRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(newSchemaStr).httpMethod(HttpRequest.PUT).requestHeaders(headers)
	.build();
		HttpResponse postResponse = HttpClientFactory.getInstance().send(httpPostRequest);

		assertEquals(201, postResponse.getCode());

		//Update with superceded by ID
		String postSchemaBody = postResponse.getBody();
		JsonElement postSchemaJsonBody = new Gson().fromJson(postSchemaBody, JsonElement.class);

		postSchemaJsonBody.getAsJsonObject().add(TestConstants.SUPERSEDED_BY, supersededByBody);

		JsonObject putRequest = new JsonObject();
		putRequest.add("schemaInfo", postSchemaJsonBody);
		putRequest.add("schema", new Gson().fromJson("{}", JsonElement.class));
		this.context.setSchemaIdFromInputPayload(newID);
		this.context.setSupersededById(supersededById);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(putRequest.toString()).httpMethod(HttpRequest.PUT).requestHeaders(headers)
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);

		this.context.setHttpResponse(response);
	}

	@Given("I hit system schema PUT API with {string} with increased minor version only")
	public void i_hit_system_schema_put_api_with_string_with_increased_minor_version_only(String inputPayload) throws Exception {
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		currentMinorVersion = currentMinorVersion + 1;
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + currentPatchVersion;
		updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit system schema PUT API with {string} with different entityType")
	public void i_hit_system_schema_put_api_with_string_with_different_entitytype(String inputPayload) throws Exception {
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + ".0";
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
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
		String newId = "SchemaSanityTest:testSource:" + entityVal + ":" + currentMajorVersion + "."
	+ currentMinorVersion + ".0";
		this.context.setSchemaIdFromInputPayload(newId);
	}

	@Given("I hit system schema PUT API with {string} with next major version")
	public void i_hit_system_schema_put_api_with_string_with_next_major_version(String inputPayload) throws Exception {

		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		currentMajorVersion = currentMajorVersion + 1;
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + ".0";
		updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
		this.context.setSchemaIdFromInputPayload(id);
		body = new Gson().toJson(jsonBody);
		Map<String, String> headers = this.context.getAuthHeaders();
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("put system schema should respond back with {string}")
	public void put_system_schema_should_respond_back_with_string(String ReponseStatusCode) {
		HttpResponse response = this.context.getHttpResponse();
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
	}

	@Given("I hit system schema PUT API for supersededBy with {string}")
	public void i_hit_system_schema_put_api_for_supersededby_with_string(String inputPayload) throws Exception {
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
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.PUT_SYSTEM_SCHEMA_ENDPOINT)
	.body(body).httpMethod(HttpRequest.PUT).requestHeaders(this.context.getAuthHeaders())
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I set request headers for {string}")
	public void i_set_request_headers_for_string(String tenant) {

		if (this.context.getAuthHeaders() != null) {
			Map<String, String> authHeaders = this.context.getAuthHeaders();
			authHeaders.put(TestConstants.DATA_PARTITION_ID, selectTenant(tenant));
			this.context.setAuthHeaders(authHeaders);
		}
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

    public void prepareSchemaParameterMapList() {
        String response = this.context.getHttpResponse().getBody();
        Gson gsn = new Gson();
        JsonObject root = gsn.fromJson(response, JsonObject.class);

        JsonObject schemaIdentity_ForEachSchemaInfo = (JsonObject) root.get("schemaIdentity");
        this.context.setSchemaVersionMajor(schemaIdentity_ForEachSchemaInfo.get("schemaVersionMajor").getAsString());
        this.context.setSchemaVersionMinor(schemaIdentity_ForEachSchemaInfo.get("schemaVersionMinor").getAsString());
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