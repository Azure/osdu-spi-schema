package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.SchemaClientExceptionSupport;
import org.opengroup.osdu.schema.util.TenantResolver;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import org.opengroup.osdu.core.test.client.model.schema.SchemaModel;

public class SchemaServiceStepDef_GET {

    @Inject
    private SchemaServiceScope context;

    String id1;
    String id2;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    List<HashMap<String, String>> list_schemaParameterMap = new ArrayList<>();

    @Given("I generate user token and set request headers for {string}")
    public void i_generate_user_token_and_set_request_headers_for_string(String tenant) {
        context.setCurrentPartitionId(TenantResolver.resolveTenant(tenant));
    }

    @Given("I get latest schema with authority, source, entityType as {string}, {string}, {string} respectively")
    public void i_get_latest_schema_with_authority_source_entitytype_as_string_string_string_respectively(String authority, String source, String entityType) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(TestConstants.AUTHORITY, authority);
        queryParams.put(TestConstants.SOURCE, source);
        queryParams.put(TestConstants.ENTITY_TYPE, entityType);
        queryParams.put(TestConstants.LATEST_VERSION, TestConstants.TRUE);

        HttpResponse<String> response = context.getSchemaClient().listSchemas(queryParams, partitionHeaders());
        context.setLastStatusCode(response.statusCode());
        context.setLastStringResponseBody(response.body());
        assertEquals(200, response.statusCode());
        LOGGER.log(Level.INFO, "resp - " + response.body());
        verifyGetListResponse(TestConstants.AUTHORITY, authority);
    }

    @Given("I hit schema service GET List API with {string} , {string} , {string}")
    public void i_hit_schema_service_get_list_api_with_string__string__string(String parameter, String parameterVal, String latestVersion) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(parameter, parameterVal);
        queryParams.put("latestVersion", latestVersion);
        HttpResponse<String> response = context.getSchemaClient().listSchemas(queryParams, partitionHeaders());
        context.setLastStatusCode(response.statusCode());
        context.setLastStringResponseBody(response.body());
        assertEquals(200, response.statusCode());
        LOGGER.log(Level.INFO, "resp - " + response.body());
        verifyGetListResponse(parameter, parameterVal);
    }

    @Given("I hit schema service GET List API with filters of {string}, {string}, {string}, {string} and getLatest flag is {string}")
    public void i_hit_schema_service_get_list_api_with_filters_of_string_string_string_string_and_getlatest_flag_is_string(String authority, String majorVersion, String minorVersion, String patchVersion, String latestVersion) {
        ArrayList<String> allValues = new ArrayList<>();
        ArrayList<String> allKeys = new ArrayList<>();
        Map<String, String> queryParams = new HashMap<>();

        if (majorVersion.equalsIgnoreCase(TestConstants.LATEST_VERSION))
            majorVersion = context.getSchemaVersionMajor();
        if (minorVersion.equalsIgnoreCase(TestConstants.LATEST_VERSION))
            minorVersion = context.getSchemaVersionMinor();
        if (patchVersion.equalsIgnoreCase(TestConstants.LATEST_VERSION))
            patchVersion = context.getSchemaVersionPatch();

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

        context.setQueryParams(queryParams);
        HttpResponse<String> response = context.getSchemaClient().listSchemas(queryParams, partitionHeaders());
        context.setLastStatusCode(response.statusCode());
        context.setLastStringResponseBody(response.body());
    }

    @Then("service should respond back with schemaInfo list from internal as well as shared scope matching {string} and {string}")
    public void service_should_respond_back_with_schemainfo_list_from_internal_as_well_as_shared_scope_matching_string_and_string(String parameter, String parameterVal) {
        verifyGetListResponse(parameter, parameterVal);
    }

    @Then("service should respond back with schemaInfo list matching {string} and {string}")
    public void service_should_respond_back_with_schemainfo_list_matching_string_and_string(String parameter, String parameterVal) {
        parameterVal = selectVersionFromInput(parameterVal);
        verifySchemaIdentityElementValues(parameter, parameterVal);
    }

    @Given("I hit schema service GET API with {string}")
    public void i_hit_schema_service_get_api_with_string(String schemaId) {
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> response = context.getSchemaClient().getSchema(schemaId, partitionHeaders());
            context.setLastStatusCode(response.statusCode());
            context.setLastSchemaModel(response.body());
        });
    }

    @Then("service should respond back with success {string} and response {string}")
    public void service_should_respond_back_with_success_string_and_response_string(String responseStatusCode, String schemaToBeVerified) throws Exception {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        String body = org.opengroup.osdu.core.test.util.TestFileUtil.readClasspathResource(schemaToBeVerified);
        Gson gsn = new Gson();
        JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
        String actualJson = new Gson().toJson(context.getLastSchemaModel().getSchema());
        JsonObject responseMsg = gsn.fromJson(actualJson, JsonObject.class);
        assertEquals(expectedData.get("schema").toString(), responseMsg.toString());
    }

    @Then("I GET updated schema")
    public void i_get_updated_schema() {
        HttpResponse<SchemaModel> response = context.getSchemaClient().getSchema(
            context.getSchemaIdFromInputPayload(), partitionHeaders());
        context.setLastStatusCode(response.statusCode());
        context.setLastSchemaModel(response.body());
        assertEquals(Integer.parseInt(TestConstants.GET_SUCCESSRESPONSECODE), response.statusCode());
    }

    @Then("I get response {string} when I try to get schema from {string} other than from where it was ingested")
    public void i_get_response_string_when_i_try_to_get_schema_from_string_other_than_from_where_it_was_ingested(String responseCode, String otherTenant) {
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> response = context.getSchemaClient().getSchema(
                context.getSchemaIdFromInputPayload(),
                Map.of("data-partition-id", otherTenant));
            context.setLastStatusCode(response.statusCode());
            context.setLastSchemaModel(response.body());
        });
        assertEquals(Integer.parseInt(responseCode), context.getLastStatusCode());
    }

    @Then("I GET udapted Schema with {string}")
    public void i_get_udapted_schema_with_string(String responseStatusCode) {
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> response = context.getSchemaClient().getSchema(
                context.getSchemaIdFromInputPayload(), partitionHeaders());
            context.setLastStatusCode(response.statusCode());
            context.setLastSchemaModel(response.body());
        });
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
    }

    @Then("schema service should respond back with {string} and {string}")
    public void schema_service_should_respond_back_with_string_and_string(String responseStatusCode, String responseToBeVerified) throws Exception {
        // The GET /schema/{id} endpoint returns only the raw schema body (not a wrapped SchemaModel),
        // so we fetch the raw string and compare it directly against the expected file.
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<String> response = context.getSchemaClient().getSchemaBody(
                context.getSchemaIdFromInputPayload(), partitionHeaders());
            context.setLastStatusCode(response.statusCode());
            context.setLastStringResponseBody(response.body());
        });
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        String body = org.opengroup.osdu.core.test.util.TestFileUtil.readClasspathResource(responseToBeVerified);
        Gson gsn = new Gson();
        JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
        JsonObject responseMsg = gsn.fromJson(context.getLastStringResponseBody(), JsonObject.class);
        assertEquals(expectedData.toString(), responseMsg.toString());
    }

    @Given("I hit schema service GET List API with {string} and {string}")
    public void i_hit_schema_service_get_list_api_with_string_and_string(String parameter, String parameterVal) {
        parameterVal = selectVersionFromInput(parameterVal);
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(parameter, parameterVal);
        HttpResponse<String> response = context.getSchemaClient().listSchemas(queryParams, partitionHeaders());
        context.setLastStatusCode(response.statusCode());
        context.setLastStringResponseBody(response.body());
    }

    @Given("I hit schema GET List API with {string} and {string}")
    public void i_hit_schema_get_list_api_with_string_and_string(String parameter, String parameterVal) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(parameter, parameterVal);
        HttpResponse<String> response = context.getSchemaClient().listSchemas(queryParams, partitionHeaders());
        context.setLastStatusCode(response.statusCode());
        context.setLastStringResponseBody(response.body());
    }

    @Given("I hit schema service GET API with blank {string}")
    public void i_hit_schema_service_get_api_with_blank_string(String header) {
        Map<String, String> overrideHeaders;
        if (header.equalsIgnoreCase("authorization")) {
            overrideHeaders = Map.of("Authorization", "");
        } else {
            overrideHeaders = Map.of("data-partition-id", "");
        }
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<String> response = context.getSchemaClient().listSchemas(Map.of(), overrideHeaders);
            context.setLastStatusCode(response.statusCode());
            context.setLastStringResponseBody(response.body());
        });
    }

    @Given("I hit schema service GET List API with query parameters having values {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}, {string}")
    public void i_hit_schema_service_get_list_api_with_query_parameters_having_values_string_string_string_string_string_string_string_string_string(String authorityVal, String sourceVal, String entityTypeVal, String statusVal, String scopeVal, String majorVersionVal, String minorVersionVal, String patchVersionVal, String count) {
        ArrayList<String> allValues = new ArrayList<>();
        ArrayList<String> allKeys = new ArrayList<>();
        Map<String, String> queryParams = new HashMap<>();

        if (majorVersionVal.equalsIgnoreCase(TestConstants.MAJOR_VERSION))
            majorVersionVal = context.getSchemaVersionMajor();
        if (minorVersionVal.equalsIgnoreCase(TestConstants.MINOR_VERSION))
            minorVersionVal = context.getSchemaVersionMinor();
        if (patchVersionVal.equalsIgnoreCase(TestConstants.PATCH_VERSION))
            patchVersionVal = context.getSchemaVersionPatch();

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

        context.setQueryParams(queryParams);
        HttpResponse<String> response = context.getSchemaClient().listSchemas(queryParams, partitionHeaders());
        context.setLastStatusCode(response.statusCode());
        context.setLastStringResponseBody(response.body());
    }

    @Then("service should respond back with {string} and schemaInfo list values matching to input")
    public void service_should_respond_back_with_string_and_schemainfo_list_values_matching_to_input(String responseCode) {
        verifyGetResponseForMultipleValues(responseCode);
    }

    @Then("service should respond back with {string} and {string} schema with correct major, minor and patch version.")
    public void service_should_respond_back_with_string_and_string_schema_with_correct_major_minor_and_patch_version(String responseStatusCode, String responseMessage) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        verifySchemaIdentityElementValues(TestConstants.SCHEMA_MAJOR_VERSION, context.getSchemaVersionMajor());
        verifySchemaIdentityElementValues(TestConstants.SCHEMA_MINOR_VERSION, context.getSchemaVersionMinor());
        verifySchemaIdentityElementValues(TestConstants.SCHEMA_PATCH_VERSION, context.getSchemaVersionPatch());
    }

    @Then("service should respond back with status code {string} or {string}")
    public void service_should_respond_back_with_status_code_string_or_string(String responseStatusCode, String alternateStatusCode) {
        int actual = context.getLastStatusCode();
        assertTrue(Integer.parseInt(responseStatusCode) == actual || Integer.parseInt(alternateStatusCode) == actual);
    }

    @Then("service should respond back with status code {string} and note down id of {string}")
    public void service_should_respond_back_with_status_code_string_and_note_down_id_of_string(String responseStatusCode, String firstSchemaOffset) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        id1 = getSchemaIdByNumber(firstSchemaOffset);
    }

    @Then("service should respond back with status code {string} and note down id of {string} and compare with earlier id")
    public void service_should_respond_back_with_status_code_string_and_note_down_id_of_string_and_compare_with_earlier_id(String responseStatusCode, String firstSchemaOffset) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        id2 = getSchemaIdByNumber(firstSchemaOffset);
        LOGGER.info("ID1 : " + id1);
        LOGGER.info("ID2 : " + id2);
        assertTrue("Offset validation is successful", id1.equals(id2));
    }

    private void verifyGetListResponse(String parameter, String parameterVal) {
        prepareSchemaParameterMapList();
        verifySchemaInfoResponse(parameter, parameterVal);
    }

    private String selectVersionFromInput(String parameterVal) {
        if (parameterVal.equalsIgnoreCase(TestConstants.MAJOR_VERSION)) {
            parameterVal = context.getSchemaVersionMajor();
        } else if (parameterVal.equalsIgnoreCase(TestConstants.MINOR_VERSION)) {
            parameterVal = context.getSchemaVersionMinor();
        } else if (parameterVal.equalsIgnoreCase(TestConstants.PATCH_VERSION)) {
            parameterVal = context.getSchemaVersionPatch();
        }
        return parameterVal;
    }

    public void prepareSchemaParameterMapList() {
        String response = context.getLastStringResponseBody();
        Gson gsn = new Gson();
        JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
        JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
        if (root.size() > 0) {
            list_schemaParameterMap.clear();
            for (JsonElement eachSchemaInfo : root) {
                Map<String, String> schemaIdentityMap = new HashMap<>();
                JsonObject schemaInfoObj = eachSchemaInfo.getAsJsonObject();
                context.setStatus(schemaInfoObj.get("status").getAsString());
                context.setScope(schemaInfoObj.get("scope").getAsString());
                JsonObject schemaIdentity = schemaInfoObj.getAsJsonObject("schemaIdentity");
                schemaIdentityMap.put("authority", schemaIdentity.get("authority").getAsString());
                schemaIdentityMap.put("source", schemaIdentity.get("source").getAsString());
                schemaIdentityMap.put("entityType", schemaIdentity.get("entityType").getAsString());
                String major = schemaIdentity.get("schemaVersionMajor").getAsString();
                String minor = schemaIdentity.get("schemaVersionMinor").getAsString();
                String patch = schemaIdentity.get("schemaVersionPatch").getAsString();
                schemaIdentityMap.put("schemaVersionMajor", major);
                schemaIdentityMap.put("schemaVersionMinor", minor);
                schemaIdentityMap.put("schemaVersionPatch", patch);
                context.setSchemaVersionMajor(major);
                context.setSchemaVersionMinor(minor);
                context.setSchemaVersionPatch(patch);
                schemaIdentityMap.put("scope", schemaInfoObj.get("scope").getAsString());
                schemaIdentityMap.put("status", schemaInfoObj.get("status").getAsString());
                list_schemaParameterMap.add((HashMap<String, String>) schemaIdentityMap);
            }
            LOGGER.log(Level.INFO, "SchemaParameterMapList - " + list_schemaParameterMap.toString());
        }
    }

    public String getSchemaIdByNumber(String offsetNo) {
        int offsetVal = Integer.parseInt(offsetNo);
        String response = context.getLastStringResponseBody();
        Gson gsn = new Gson();
        JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
        JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
        String offsetValId = null;
        if (root.size() > 0) {
            JsonObject schemaIdentity = root.get(offsetVal).getAsJsonObject().getAsJsonObject("schemaIdentity");
            offsetValId = schemaIdentity.get("id").getAsString();
            LOGGER.log(Level.INFO, "SchemaOffsetNumberId is - " + offsetValId);
        }
        return offsetValId;
    }

    private void verifySchemaInfoResponse(String parameterName, String parameterVal) {
        for (HashMap<String, String> schemaInfoMap : list_schemaParameterMap) {
            assertEquals(
                "Response schemaInfoList contains schemaInfo not matching parameter criteria - " + parameterName,
                parameterVal, schemaInfoMap.get(parameterName));
        }
    }

    public void verifySchemaIdentityElementValues(String parameter, String value) {
        String response = context.getLastStringResponseBody();
        Gson gsn = new Gson();
        JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
        JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
        verifyResponseJsonElement(parameter, value, root);
    }

    private void verifyResponseJsonElement(String parameter, String value, JsonArray root) {
        for (JsonElement eachSchemaInfo : root) {
            if (parameter.equalsIgnoreCase("status") || parameter.equalsIgnoreCase("scope")) {
                String actualVal = eachSchemaInfo.getAsJsonObject().get(parameter).getAsString();
                assertEquals(actualVal, value);
            } else {
                JsonObject schemaIdentity = eachSchemaInfo.getAsJsonObject().getAsJsonObject("schemaIdentity");
                String actualVal = schemaIdentity.get(parameter).getAsString();
                assertEquals(actualVal, value);
            }
        }
    }

    public void verifyGetResponseForMultipleValues(String responseCode) {
        assertEquals(Integer.parseInt(responseCode), context.getLastStatusCode());
        Map<String, String> queryParams = context.getQueryParams();
        String response = context.getLastStringResponseBody();
        Gson gsn = new Gson();
        JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
        JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
        queryParams.entrySet().forEach(entry -> verifyResponseJsonElement(entry.getKey(), entry.getValue(), root));
    }

    private Map<String, String> partitionHeaders() {
        String partition = context.getCurrentPartitionId();
        if (partition == null || partition.isEmpty()) {
            return Map.of();
        }
        return Map.of("data-partition-id", partition);
    }
}
