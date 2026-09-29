package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.schema.SchemaModel;
import org.opengroup.osdu.core.test.util.TestFileUtil;
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

public class SystemSchemaStepDef_PUT {

    @Inject
    private SchemaServiceScope context;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /** Headers for system API calls — clears the auto-injected data-partition-id. */
    private Map<String, String> systemHeaders() {
        return Map.of("data-partition-id", "");
    }

    @Given("I generate user token and set request headers for system API")
    public void i_generate_user_token_and_set_request_headers_for_system_api() {
        context.setCurrentPartitionId(null);
    }

    @Given("I set request headers for {string}")
    public void i_set_request_headers_for_string(String tenant) {
        context.setCurrentPartitionId(TenantResolver.resolveTenant(tenant));
    }

    @Given("I hit system schema PUT API with {string} only if status is not development")
    public void i_hit_system_schema_put_api_with_string_only_if_status_is_not_development(String inputPayload) throws IOException {
        String resp = context.getLastStringResponseBody();
        Gson gsn = new Gson();
        JsonObject schemaInfosList = gsn.fromJson(resp, JsonObject.class);
        JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
        if (!"DEVELOPMENT".equals(context.getStatus()) || (root.size() == 0) || "INTERNAL".equals(context.getScope())) {
            String body = TestFileUtil.readClasspathResource(inputPayload);
            JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
            int currentMinorVersion = 0;
            int currentMajorVersion = 0;
            int currentPatchVersion = 0;
            if (root.size() > 0) {
                currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
                currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
                currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
            }
            int nextMinorVersion = currentMinorVersion + 1;
            int nextMajorVersion = currentMajorVersion + 1;
            String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
                + nextMinorVersion + "." + currentPatchVersion;
            context.setSchemaIdFromInputPayload(schemaId);
            updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, currentPatchVersion, schemaId);
            systemPutRequest(jsonBody, schemaId);
            assertTrue(context.getLastStatusCode() == 200 || context.getLastStatusCode() == 201);
            prepareSchemaParameterMapList();
        }
    }

    @Given("I hit system schema PUT API with {string} and mark schema as {string}.")
    public void i_hit_system_schema_put_api_with_string_and_mark_schema_as_string(String inputPayload, String status) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
        context.setSchemaIdFromInputPayload(id);
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().remove("status");
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().addProperty("status", status);
        systemPutRequest(jsonBody, id);
    }

    @Given("I hit system schema PUT API with {string} and mark schema as {string} for next major version")
    public void i_hit_system_schema_put_api_with_string_and_mark_schema_as_string_for_next_major_version(String inputPayload, String status) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor()) + 1;
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + ".0";
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
        context.setSchemaIdFromInputPayload(id);
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().remove("status");
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject().addProperty("status", status);
        systemPutRequest(jsonBody, id);
    }

    @Given("I hit system schema PUT API with {string}")
    public void i_hit_system_schema_put_api_with_string(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
        context.setSchemaIdFromInputPayload(id);
        systemPutRequest(jsonBody, id);
    }

    @Given("I hit system schema PUT API with {string} for superceded input")
    public void i_hit_system_schema_put_api_with_string_for_superceded_input(String inputPayload) throws IOException {
        String newSchemaStr = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement newSchemaJsonBody = new Gson().fromJson(newSchemaStr, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int patchMajorVersion = Integer.parseInt(context.getSchemaVersionPatch());

        // Read supersededBy identity from the last GET list response
        String latestSchemaResp = context.getLastStringResponseBody();
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

        // PUT new system schema (idempotent: 201 on first run, 200 on re-run)
        SchemaModel newSchema = SchemaClientExceptionSupport.buildModel(newSchemaJsonBody);
        HttpResponse<SchemaModel> postResponse = context.getSchemaClient()
            .createSystemSchemaAndGetInfo(newSchema, systemHeaders());
        assertTrue(postResponse.statusCode() == 200 || postResponse.statusCode() == 201);

        // Build PUT body with supersededBy
        JsonElement postSchemaJsonBody = new Gson().fromJson(
            new Gson().toJson(postResponse.body().getSchemaInfo()), JsonElement.class);
        postSchemaJsonBody.getAsJsonObject().add(TestConstants.SUPERSEDED_BY, supersededByBody);

        JsonObject putRequestBody = new JsonObject();
        putRequestBody.add("schemaInfo", postSchemaJsonBody);
        putRequestBody.add("schema", new Gson().fromJson("{}", JsonElement.class));

        context.setSchemaIdFromInputPayload(newID);
        context.setSupersededById(supersededById);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> r = context.getSchemaClient().createSystemSchemaAndGetInfo(
                SchemaClientExceptionSupport.buildModel(putRequestBody),
                systemHeaders());
            context.setLastStatusCode(r.statusCode());
            context.setLastSchemaModel(r.body());
        });
    }

    @Given("I hit system schema PUT API with {string} with increased minor version only")
    public void i_hit_system_schema_put_api_with_string_with_increased_minor_version_only(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor()) + 1;
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
        context.setSchemaIdFromInputPayload(id);
        systemPutRequest(jsonBody, id);
    }

    @Given("I hit system schema PUT API with {string} with different entityType")
    public void i_hit_system_schema_put_api_with_string_with_different_entitytype(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + ".0";
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
        context.setSchemaIdFromInputPayload(id);
        int randomNum = (int) (Math.random() * 10000);
        String entityVal = "testEntity" + randomNum;
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
            .remove("entityType");
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
            .addProperty("entityType", entityVal);
        systemPutRequest(jsonBody, id);
        String newId = "SchemaSanityTest:testSource:" + entityVal + ":" + currentMajorVersion + "."
            + currentMinorVersion + ".0";
        context.setSchemaIdFromInputPayload(newId);
    }

    @Given("I hit system schema PUT API with {string} with next major version")
    public void i_hit_system_schema_put_api_with_string_with_next_major_version(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor()) + 1;
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + ".0";
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
        context.setSchemaIdFromInputPayload(id);
        systemPutRequest(jsonBody, id);
    }

    @Then("put system schema should respond back with {string}")
    public void put_system_schema_should_respond_back_with_string(String responseStatusCode) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
    }

    @Given("I hit system schema PUT API for supersededBy with {string}")
    public void i_hit_system_schema_put_api_for_supersededby_with_string(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String supersededById = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updateSupersededByInJsonBody(jsonBody, supersededById, currentMajorVersion, currentMinorVersion, currentPatchVersion);
        context.setSupersededById(supersededById);
        int nextMinorVersion = currentMinorVersion + 1;
        int nextMajorVersion = currentMajorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
            + nextMinorVersion + ".0";
        context.setSchemaIdFromInputPayload(schemaId);
        updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, schemaId);
        systemPutRequest(jsonBody, schemaId);
    }

    // ------ private helpers ------

    private void systemPutRequest(JsonElement jsonBody, String schemaId) {
        context.setSchemaIdFromInputPayload(schemaId);
        SchemaModel schema = SchemaClientExceptionSupport.buildModel(jsonBody);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> r = context.getSchemaClient().createSystemSchemaAndGetInfo(
                schema, systemHeaders());
            context.setLastStatusCode(r.statusCode());
            context.setLastSchemaModel(r.body());
        });
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
        String schemaId = context.getSchemaIdFromInputPayload();
        String[] parts = schemaId.split(":");
        if (parts.length >= 4) {
            String[] versions = parts[3].split("\\.");
            if (versions.length >= 3) {
                context.setSchemaVersionMajor(versions[0]);
                context.setSchemaVersionMinor(versions[1]);
                context.setSchemaVersionPatch(versions[2]);
            }
        }
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
}
