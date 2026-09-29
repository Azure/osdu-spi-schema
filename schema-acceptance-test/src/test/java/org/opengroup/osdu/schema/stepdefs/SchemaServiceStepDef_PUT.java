package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.schema.SchemaModel;
import org.opengroup.osdu.core.test.util.TestFileUtil;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.SchemaClientExceptionSupport;
import org.opengroup.osdu.schema.util.TenantResolver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class SchemaServiceStepDef_PUT {

    @Inject
    private SchemaServiceScope context;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} and mark schema as {string}")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_and_mark_schema_as_string(String inputPayload, String tenant, String status) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
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
        putRequest(jsonBody, id, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} and mark schema as {string} for next major version")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_and_mark_schema_as_string_for_next_major_version(String inputPayload, String tenant, String status) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
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
        putRequest(jsonBody, id, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string}")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
        context.setSchemaIdFromInputPayload(id);
        putRequest(jsonBody, id, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} for superceded input")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_for_superceded_input(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
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

        // First PUT to create the new schema (idempotent: 201 on first run, 200 on re-run)
        SchemaModel newSchema = SchemaClientExceptionSupport.buildModel(newSchemaJsonBody);
        HttpResponse<SchemaModel> postResponse = context.getSchemaClient().createAndGetInfo(
            newSchema, Map.of("data-partition-id", finalTenant));
        assertTrue(postResponse.statusCode() == 200 || postResponse.statusCode() == 201);

        // Build PUT body with supersededBy
        JsonElement postSchemaJsonBody = new Gson().fromJson(new Gson().toJson(postResponse.body().getSchemaInfo()), JsonElement.class);
        postSchemaJsonBody.getAsJsonObject().add(TestConstants.SUPERSEDED_BY, supersededByBody);

        JsonObject putRequestBody = new JsonObject();
        putRequestBody.add("schemaInfo", postSchemaJsonBody);
        putRequestBody.add("schema", new Gson().fromJson("{}", JsonElement.class));

        context.setSchemaIdFromInputPayload(newID);
        context.setSupersededById(supersededById);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> r = context.getSchemaClient().createAndGetInfo(
                SchemaClientExceptionSupport.buildModel(putRequestBody),
                Map.of("data-partition-id", finalTenant));
            context.setLastStatusCode(r.statusCode());
            context.setLastSchemaModel(r.body());
        });
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased patch version only")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_patch_version_only(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int nextPatchVersion = currentPatchVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + nextPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, nextPatchVersion, schemaId);
        putRequest(jsonBody, schemaId, finalTenant);
    }

    @Given("I hit schema service PUT API with {string} and data-partition-id as {string} with increased minor version with 2 count")
    public void i_hit_schema_service_put_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_with_2_count(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int nextMinorVersion = currentMinorVersion + 2;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + currentPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
        putRequest(jsonBody, schemaId, finalTenant);
        prepareSchemaParameterMapList(schemaId);
    }

    @Given("I hit schema service PUT API with {string} and data-partition-id as {string} with less minor version by 1 count than earlier")
    public void i_hit_schema_service_put_api_with_string_and_datapartitionid_as_string_with_less_minor_version_by_1_count_than_earlier(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int previousMinorVersion = currentMinorVersion - 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + previousMinorVersion + ".0";
        context.setSchemaIdFromInputPayload(schemaId);
        updateVersionInJsonBody(jsonBody, previousMinorVersion, currentMajorVersion, schemaId);
        putRequest(jsonBody, schemaId, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased minor version and patch version")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_minor_version_and_patch_version(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int nextMinorVersion = currentMinorVersion + 1;
        int nextPatchVersion = currentPatchVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + nextPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, nextPatchVersion, schemaId);
        putRequest(jsonBody, schemaId, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased minor version")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_minor_version(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + currentPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updateVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, schemaId);
        putRequest(jsonBody, schemaId, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} with increased minor version only")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_increased_minor_version_only(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor()) + 1;
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
        context.setSchemaIdFromInputPayload(id);
        putRequest(jsonBody, id, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} with different entityType")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_different_entitytype(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
        context.setSchemaIdFromInputPayload(id);
        int randomNum = (int) (Math.random() * 10000);
        String entityVal = "testEntity" + randomNum;
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
            .remove("entityType");
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").get("schemaIdentity").getAsJsonObject()
            .addProperty("entityType", entityVal);
        putRequest(jsonBody, id, finalTenant);
    }

    @Given("I hit schema service PUT API with {string}, data-partition-id as {string} with next major version")
    public void i_hit_schema_service_put_api_with_string_datapartitionid_as_string_with_next_major_version(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor()) + 1;
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, id);
        context.setSchemaIdFromInputPayload(id);
        putRequest(jsonBody, id, finalTenant);
    }

    @Then("put schema service should respond back with {string}")
    public void put_schema_service_should_respond_back_with_string(String responseStatusCode) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
    }

    @Then("the put service for supersededBy should respond back with {string}")
    public void the_put_service_for_supersededby_should_respond_back_with_string(String responseStatusCode) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        // The PUT response returns SchemaInfo (flat JSON) which includes supersededBy.
        // context.getLastSchemaModel() holds the SchemaInfo captured from the PUT response.
        SchemaModel model = context.getLastSchemaModel();
        Assert.assertNotNull(model.getSchemaInfo().getSchemaIdentity());
        assertEquals(context.getSchemaIdFromInputPayload(), model.getSchemaInfo().getSchemaIdentity().getId());
        String rawSchemaInfo = new Gson().toJson(model.getSchemaInfo());
        JsonObject schemaInfoJson = new Gson().fromJson(rawSchemaInfo, JsonObject.class);
        Assert.assertNotNull(schemaInfoJson.get(TestConstants.SUPERSEDED_BY));
        assertEquals(context.getSupersededById(),
            schemaInfoJson.getAsJsonObject(TestConstants.SUPERSEDED_BY).get(TestConstants.ID).getAsString());
    }

    @Given("I hit schema service PUT API for supersededBy with {string} and data-partition-id as {string}")
    public void i_hit_schema_service_put_api_for_supersededby_with_string_and_datapartitionid_as_string(String inputPayload, String tenant) throws IOException {
        final String finalTenant = TenantResolver.resolveTenant(tenant);
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
        SchemaClientExceptionSupport.tryRun(context, () -> {
            SchemaModel schema = SchemaClientExceptionSupport.buildModel(jsonBody);
            HttpResponse<SchemaModel> r = context.getSchemaClient().createAndGetInfo(
                schema, Map.of("data-partition-id", finalTenant));
            context.setLastStatusCode(r.statusCode());
            context.setLastSchemaModel(r.body());
        });
    }

    private void putRequest(JsonElement jsonBody, String schemaId, String tenant) {
        context.setSchemaIdFromInputPayload(schemaId);
        JsonElement schemaEl = jsonBody.getAsJsonObject().get(TestConstants.SCHEMA);
        context.setSchemaFromInputPayload(schemaEl != null ? schemaEl.toString() : null);
        context.setJsonPayloadForPostPUT(jsonBody.toString());
        SchemaModel schema = SchemaClientExceptionSupport.buildModel(jsonBody);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> r = context.getSchemaClient().createAndGetInfo(
                schema, Map.of("data-partition-id", tenant));
            context.setLastStatusCode(r.statusCode());
            context.setLastSchemaModel(r.body());
        });
    }

    public void prepareSchemaParameterMapList(String schemaId) {
        // Extract versions from the stored schema ID (format: authority:source:entity:major.minor.patch)
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
