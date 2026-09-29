package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.AuthUtil;
import org.opengroup.osdu.schema.util.HttpClientFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class SchemaServiceStepDef_GET {

	@Inject
	private SchemaServiceScope context;

	static String[] GetListBaseFilterArray;
	static String[] GetListVersionFilterArray;
	String queryParameter;
	String id1;
	String id2;
	private static TreeSet<String> LIST_OF_AVAILABLE_SCHEMAS = new TreeSet<String>();

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	List<HashMap<String, String>> list_schemaParameterMap = new ArrayList<HashMap<String, String>>();

	@Given("I generate user token and set request headers for {string}")
	public void i_generate_user_token_and_set_request_headers_for_string(String tenant) throws Exception {
		if (this.context.getToken() == null) {
			String token = new AuthUtil().getToken();
			this.context.setToken(token);
		}

		if (this.context.getAuthHeaders() == null) {
			Map<String, String> authHeaders = new HashMap<String, String>();
			authHeaders.put(TestConstants.AUTHORIZATION, this.context.getToken());
			authHeaders.put(TestConstants.DATA_PARTITION_ID, selectTenant(tenant));
			authHeaders.put(TestConstants.CONTENT_TYPE, TestConstants.JSON_CONTENT);
			this.context.setAuthHeaders(authHeaders);
		}
	}

	@Given("I get latest schema with authority, source, entityType as {string}, {string}, {string} respectively")
	public void i_get_latest_schema_with_authority_source_entitytype_as_string_string_string_respectively(String authority, String source, String entityType) throws Exception {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(TestConstants.AUTHORITY, authority);
		queryParams.put(TestConstants.SOURCE, source);
		queryParams.put(TestConstants.ENTITY_TYPE, entityType);
		queryParams.put(TestConstants.LATEST_VERSION, TestConstants.TRUE);

		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_LIST_ENDPOINT).queryParams(queryParams)
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
		assertEquals("200", String.valueOf(response.getCode()));
		verifyGetListResponse(TestConstants.AUTHORITY, authority);
	}

	@Given("I hit schema service GET List API with {string} , {string} , {string}")
	public void i_hit_schema_service_get_list_api_with_string__string__string(String parameter, String parameterVal, String latestVersion) throws Exception {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(parameter, parameterVal);
		queryParams.put("latestVersion", latestVersion);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_LIST_ENDPOINT).queryParams(queryParams)
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
		assertEquals("200", String.valueOf(response.getCode()));
		verifyGetListResponse(parameter, parameterVal);
	}

	@Given("I hit schema service GET List API with filters of {string}, {string}, {string}, {string} and getLatest flag is {string}")
	public void i_hit_schema_service_get_list_api_with_filters_of_string_string_string_string_and_getlatest_flag_is_string(String authority, String majorVersion, String minorVersion, String patchVersion, String latestVersion) {
		ArrayList<String> allValues = new ArrayList<>();
		ArrayList<String> allKeys = new ArrayList<>();
		Map<String, String> queryParams = new HashMap<String, String>();

		if (majorVersion.equalsIgnoreCase(TestConstants.LATEST_VERSION))
			majorVersion = this.context.getSchemaVersionMajor();
		if (minorVersion.equalsIgnoreCase(TestConstants.LATEST_VERSION))
			minorVersion = this.context.getSchemaVersionMinor();
		if (patchVersion.equalsIgnoreCase(TestConstants.LATEST_VERSION))
			patchVersion = this.context.getSchemaVersionPatch();

		allValues.add(majorVersion);
		allValues.add(minorVersion);
		allValues.add(patchVersion);

		allKeys.add(TestConstants.SCHEMA_MAJOR_VERSION);
		allKeys.add(TestConstants.SCHEMA_MINOR_VERSION);
		allKeys.add(TestConstants.SCHEMA_PATCH_VERSION);

		for (int i = 0; i < allKeys.size(); i++) {
			if (!allValues.get(i).equalsIgnoreCase("NA")) {
				queryParams.put(allKeys.get(i), allValues.get(i));
			}
		}

		queryParams.put(TestConstants.AUTHORITY, authority);
		queryParams.put(TestConstants.LATEST_VERSION, latestVersion);

		this.context.setQueryParams(queryParams);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_LIST_ENDPOINT).queryParams(queryParams)
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("service should respond back with schemaInfo list from internal as well as shared scope matching {string} and {string}")
	public void service_should_respond_back_with_schemainfo_list_from_internal_as_well_as_shared_scope_matching_string_and_string(String parameter, String parameterVal) throws Exception {
		verifyGetListResponse(parameter, parameterVal);
	}

	@Then("service should respond back with schemaInfo list matching {string} and {string}")
	public void service_should_respond_back_with_schemainfo_list_matching_string_and_string(String parameter, String parameterVal) throws Exception {

		parameterVal = selectVersionFromInput(parameterVal);
		verifySchemaIdentityElementValues(parameter, parameterVal);
	}

	@Given("I hit schema service GET API with {string}")
	public void i_hit_schema_service_get_api_with_string(String schemaId) {
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_ENDPOINT + schemaId).httpMethod(HttpRequest.GET)
	.requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("service should respond back with success {string} and response {string}")
	public void service_should_respond_back_with_success_string_and_response_string(String ReponseStatusCode, String SchemaToBeVerified) throws Exception {
		HttpResponse response = this.context.getHttpResponse();
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
		String body = this.context.getFileUtils().read(SchemaToBeVerified);
		Gson gsn = new Gson();
		JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
		JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
		assertEquals(expectedData.get("schema").toString(), responseMsg.get("schema").toString());
	}

	@Then("I GET updated schema")
	public void i_get_updated_schema() {
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_ENDPOINT + this.context.getSchemaIdFromInputPayload())
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
		assertEquals(TestConstants.GET_SUCCESSRESPONSECODE, String.valueOf(response.getCode()));
	}

	@Then("I get response {string} when I try to get schema from {string} other than from where it was ingested")
	public void i_get_response_string_when_i_try_to_get_schema_from_string_other_than_from_where_it_was_ingested(String responseCode, String otherTenant) {
		Map<String, String> authHeaders = this.context.getAuthHeaders();
		authHeaders.put(TestConstants.DATA_PARTITION_ID, otherTenant);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_ENDPOINT
+ this.context.getSchemaIdFromInputPayload())
	.httpMethod(HttpRequest.GET).requestHeaders(authHeaders).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
		assertEquals(responseCode, String.valueOf(response.getCode()));
	}

	@Then("I GET udapted Schema with {string}")
	public void i_get_udapted_schema_with_string(String ReponseStatusCode) {
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_ENDPOINT + this.context.getSchemaIdFromInputPayload())
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
	}

	@Then("schema service should respond back with {string} and {string}")
	public void schema_service_should_respond_back_with_string_and_string(String ReponseStatusCode, String ResponseToBeVerified) throws Exception {
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_ENDPOINT
+ this.context.getSchemaIdFromInputPayload())
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
		String body = this.context.getFileUtils().read(ResponseToBeVerified);
		Gson gsn = new Gson();
		JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
		JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
		assertEquals(expectedData.toString(), responseMsg.toString());
	}

	@Given("I hit schema service GET List API with {string} and {string}")
	public void i_hit_schema_service_get_list_api_with_string_and_string(String parameter, String parameterVal) {

		parameterVal = selectVersionFromInput(parameterVal);

		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(parameter, parameterVal);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_LIST_ENDPOINT).queryParams(queryParams)
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema GET List API with {string} and {string}")
	public void i_hit_schema_get_list_api_with_string_and_string(String parameter, String parameterVal) {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(parameter, parameterVal);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_LIST_ENDPOINT).queryParams(queryParams)
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service GET API with blank {string}")
	public void i_hit_schema_service_get_api_with_blank_string(String header) {
		Map<String, String> authHeaders = this.context.getAuthHeaders();

		if (header.equalsIgnoreCase("authorization")) {
			authHeaders.put(TestConstants.AUTHORIZATION, "");
		} else {
			authHeaders.put(TestConstants.DATA_PARTITION_ID, "");
		}

		Map<String, String> queryParams = new HashMap<String, String>();
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.GET_LIST_ENDPOINT)
	.queryParams(queryParams).httpMethod(HttpRequest.GET).requestHeaders(authHeaders).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service GET List API with query parameters having values {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}")
	public void i_hit_schema_service_get_list_api_with_query_parameters_having_values_string_string_string_string_string_string_string_string_string(String authorityVal, String sourceVal, String entityTypeVal, String statusVal, String scopeVal, String majorVersionVal, String minorVersionVal, String patchVersionVal, String count) {
		ArrayList<String> allValues = new ArrayList<>();
		ArrayList<String> allKeys = new ArrayList<>();
		Map<String, String> queryParams = new HashMap<String, String>();

		if (majorVersionVal.equalsIgnoreCase(TestConstants.MAJOR_VERSION))
			majorVersionVal = this.context.getSchemaVersionMajor();
		if (minorVersionVal.equalsIgnoreCase(TestConstants.MINOR_VERSION))
			minorVersionVal = this.context.getSchemaVersionMinor();
		if (patchVersionVal.equalsIgnoreCase(TestConstants.PATCH_VERSION))
			patchVersionVal = this.context.getSchemaVersionPatch();

		allValues.add(authorityVal);
		allValues.add(sourceVal);
		allValues.add(entityTypeVal);
		allValues.add(statusVal);
		allValues.add(scopeVal);
		allValues.add(majorVersionVal);
		allValues.add(minorVersionVal);
		allValues.add(patchVersionVal);

		allKeys.add("authority");
		allKeys.add("source");
		allKeys.add("entityType");
		allKeys.add("status");
		allKeys.add("scope");
		allKeys.add("schemaVersionMajor");
		allKeys.add("schemaVersionMinor");
		allKeys.add("schemaVersionPatch");

		for (int i = 0; i < allKeys.size(); i++) {
			if (!allValues.get(i).equalsIgnoreCase("NA")) {
				queryParams.put(allKeys.get(i), allValues.get(i));
			}
		}

		this.context.setQueryParams(queryParams);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_LIST_ENDPOINT).queryParams(queryParams)
	.httpMethod(HttpRequest.GET).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("service should respond back with {string} and schemaInfo list values matching to input")
	public void service_should_respond_back_with_string_and_schemainfo_list_values_matching_to_input(String responseCode) throws Exception {
		verifyGetResponseForMultipleValues(responseCode);
	}

	@Then("service should respond back with {string} and {string} schema with correct major, minor and patch version.")
	public void service_should_respond_back_with_string_and_string_schema_with_correct_major_minor_and_patch_version(String ReponseStatusCode, String ResponseMessage) throws Exception {
		String body = this.context.getFileUtils().read(ResponseMessage);
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
		HttpResponse response = this.context.getHttpResponse();

		if (response != null) {
			assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));

			verifySchemaIdentityElementValues(TestConstants.SCHEMA_MAJOR_VERSION,
		this.context.getSchemaVersionMajor());
			verifySchemaIdentityElementValues(TestConstants.SCHEMA_MINOR_VERSION,
		this.context.getSchemaVersionMinor());
			verifySchemaIdentityElementValues(TestConstants.SCHEMA_PATCH_VERSION,
		this.context.getSchemaVersionPatch());
		}
	}

	@Then("service should respond back with status code {string} or {string}")
	public void service_should_respond_back_with_status_code_string_or_string(String ResponseStatusCode, String AlternateStatusCode) {
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertTrue(ResponseStatusCode.equals(String.valueOf(response.getCode()))
		|| AlternateStatusCode.equals(String.valueOf(response.getCode())));
		}
	}

	@Then("service should respond back with status code {string} and note down id of {string}")
	public void service_should_respond_back_with_status_code_string_and_note_down_id_of_string(String ResponseStatusCode, String firstschemaOffset) throws Exception {
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertTrue(ResponseStatusCode.equals(String.valueOf(response.getCode())));
		}
		id1 = getSchemaIdByNumber(firstschemaOffset);
	}

	@Then("service should respond back with status code {string} and note down id of {string} and compare with earlier id")
	public void service_should_respond_back_with_status_code_string_and_note_down_id_of_string_and_compare_with_earlier_id(String ResponseStatusCode, String firstschemaOffset) throws Exception {
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertTrue(ResponseStatusCode.equals(String.valueOf(response.getCode())));
		}
		id2 = getSchemaIdByNumber(firstschemaOffset);
		LOGGER.info("ID1 : " + id1);
		LOGGER.info("ID : " + id2);
		assertTrue("Offset validation is successful", id1.equals(id2));
	}

	private void verifyGetListResponse(String parameter, String parameterVal) throws IOException {
		prepareSchemaParameterMapList();
		verifySchemaInfoResponse(parameter, parameterVal);
	}

	private String selectVersionFromInput(String parameterVal) {
		if (parameterVal.equalsIgnoreCase(TestConstants.MAJOR_VERSION)) {
			parameterVal = this.context.getSchemaVersionMajor();
		} else if (parameterVal.equalsIgnoreCase(TestConstants.MINOR_VERSION)) {
			parameterVal = this.context.getSchemaVersionMinor();
		} else if (parameterVal.equalsIgnoreCase(TestConstants.PATCH_VERSION)) {
			parameterVal = this.context.getSchemaVersionPatch();
		}
		return parameterVal;
	}

	public void prepareSchemaParameterMapList() {
		String response = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
		JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
		if (root.size() > 0) {
			for (JsonElement eachSchemaInfo : root) {
				Map<String, String> schemaIdentityMap = new HashMap<String, String>();

				JsonObject schemaIdentity_ForEachSchemaStatus = (JsonObject) (eachSchemaInfo.getAsJsonObject());

				this.context.setStatus(schemaIdentity_ForEachSchemaStatus.get("status").getAsString());
				this.context.setScope(schemaIdentity_ForEachSchemaStatus.get("scope").getAsString());

				JsonObject schemaIdentity_ForEachSchemaInfo = (JsonObject) eachSchemaInfo.getAsJsonObject()
						.get("schemaIdentity");
				schemaIdentityMap.put("authority", schemaIdentity_ForEachSchemaInfo.get("authority").getAsString());
				schemaIdentityMap.put("source", schemaIdentity_ForEachSchemaInfo.get("source").getAsString());
				schemaIdentityMap.put("entityType", schemaIdentity_ForEachSchemaInfo.get("entityType").getAsString());

				schemaIdentityMap.put("schemaVersionMajor",
						schemaIdentity_ForEachSchemaInfo.get("schemaVersionMajor").getAsString());
				this.context.setSchemaVersionMajor(
						schemaIdentity_ForEachSchemaInfo.get("schemaVersionMajor").getAsString());

				schemaIdentityMap.put("schemaVersionMinor",
						schemaIdentity_ForEachSchemaInfo.get("schemaVersionMinor").getAsString());
				this.context.setSchemaVersionMinor(
						schemaIdentity_ForEachSchemaInfo.get("schemaVersionMinor").getAsString());

				schemaIdentityMap.put("schemaVersionPatch",
						schemaIdentity_ForEachSchemaInfo.get("schemaVersionPatch").getAsString());
				this.context.setSchemaVersionPatch(
						schemaIdentity_ForEachSchemaInfo.get("schemaVersionPatch").getAsString());

				schemaIdentityMap.put("scope", eachSchemaInfo.getAsJsonObject().get("scope").getAsString());
				schemaIdentityMap.put("status", eachSchemaInfo.getAsJsonObject().get("status").getAsString());
				this.list_schemaParameterMap.add((HashMap<String, String>) schemaIdentityMap);
			}
			LOGGER.log(Level.INFO, "SchemaParameterMapList - " + this.list_schemaParameterMap.toString());
		}
	}

	public String getSchemaIdByNumber(String offsetNo) {
		int offsetVal = Integer.parseInt(offsetNo);
		String response = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		String offsetValId = null;
		JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
		JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
		if (root.size() > 0) {
			JsonObject schemaIdentity_ForEachSchemaStatus = (JsonObject) (root.get(offsetVal).getAsJsonObject());
			JsonObject schemaIdentity_ForEachSchemaInfo = (JsonObject) root.get(offsetVal).getAsJsonObject()
					.get("schemaIdentity");
			offsetValId = schemaIdentity_ForEachSchemaInfo.get("id").getAsString();
			LOGGER.log(Level.INFO, "SchemaOffsetNumberId is - " + offsetValId);
		}
		return offsetValId;
	}

	private void verifySchemaInfoResponse(String parameterName, String parameterVal) {
		for (HashMap<String, String> schemaInfoMap : this.list_schemaParameterMap) {
			assertEquals(
					"Response schemaInfoList contains schemaInfo not matching parameter criteria - " + parameterName,
					parameterVal.toString(), schemaInfoMap.get(parameterName).toString());
		}
	}

	public void verifySchemaIdentityElementValues(String parameter, String value) {
		String response = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
		JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
		verifyResponseJsonElement(parameter, value, root);
	}

	private void verifyResponseJsonElement(String parameter, String value, JsonArray root) {
		for (JsonElement eachSchemaInfo : root) {

			if (parameter.equalsIgnoreCase("status") || parameter.equalsIgnoreCase("scope")) {
				String ActualVal = eachSchemaInfo.getAsJsonObject().get(parameter).getAsString();
				assertEquals(ActualVal, value);
			} else {
				JsonObject schemaIdentity_ForEachSchemaInfo = (JsonObject) eachSchemaInfo.getAsJsonObject()
						.get("schemaIdentity");
				String ActualVal = schemaIdentity_ForEachSchemaInfo.get(parameter).getAsString();
				assertEquals(ActualVal, value);
			}

		}
	}

	public void verifyGetResponseForMultipleValues(String responseCode) {
		int respCode = this.context.getHttpResponse().getCode();
		assertEquals(responseCode, String.valueOf(respCode));
		Map<String, String> queryParams = this.context.getQueryParams();
		String response = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
		JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");

		queryParams.entrySet().stream().forEach(entry -> {
			String key = entry.getKey();
			String value = entry.getValue();
			verifyResponseJsonElement(key, value, root);
		});
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