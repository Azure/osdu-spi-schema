package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;

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
            this.context.setAuthHeaders(authHeaders);
        }
    }

    @Given("I get latest schema with authority, source, entityType as {string}, {string}, {string} respectively")
    public void i_get_latest_schema_with_authority_source_entitytype_as_string_string_string_respectively(String authority, String source, String entityType) throws IOException {
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

    @Then("schema service should respond back with {string} and {string}")
    public void schema_service_should_respond_back_with_string_and_string(String ReponseStatusCode, String ResponseToBeVerified) throws IOException {
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

    private void verifyGetListResponse(String parameter, String parameterVal) throws IOException {
        prepareSchemaParameterMapList();
        verifySchemaInfoResponse(parameter, parameterVal);
    }

    public void prepareSchemaParameterMapList() throws IOException {
        String response = this.context.getHttpResponse().getBody();
        Gson gsn = new Gson();
        JsonObject schemaInfosList = gsn.fromJson(response, JsonObject.class);
        JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
        if (root.size() > 0) {
            for (JsonElement eachSchemaInfo : root) {
                Map<String, String> schemaIdentityMap = new HashMap<String, String>();

                JsonObject schemaIdentity_ForEachSchemaStatus = (JsonObject) (eachSchemaInfo.getAsJsonObject());

                this.context.setStatus(schemaIdentity_ForEachSchemaStatus.get("status").getAsString());

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

    private void verifySchemaInfoResponse(String parameterName, String parameterVal) {
        for (HashMap<String, String> schemaInfoMap : this.list_schemaParameterMap) {
            assertEquals(
                    "Response schemaInfoList contains schemaInfo not matching parameter criteria - " + parameterName,
                    parameterVal.toString(), schemaInfoMap.get(parameterName).toString());
        }
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