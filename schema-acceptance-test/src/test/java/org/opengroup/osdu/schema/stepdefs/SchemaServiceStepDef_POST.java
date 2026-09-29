package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.HttpResponse;
import org.opengroup.osdu.core.test.client.model.schema.SchemaIdentity;
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

public class SchemaServiceStepDef_POST {

    @Inject
    private SchemaServiceScope context;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    List<HashMap<String, String>> list_schemaParameterMap = new ArrayList<>();

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} only if status is not development")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_only_if_status_is_not_development(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String resp = context.getLastStringResponseBody();
        Gson gsn = new Gson();
        JsonObject schemaInfosList = gsn.fromJson(resp, JsonObject.class);
        JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
        if (!"DEVELOPMENT".equals(context.getStatus()) || (root.size() == 0)) {
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
            int nextPatchVersion = currentPatchVersion + 1;
            String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
                + nextMinorVersion + "." + nextPatchVersion;
            context.setSchemaIdFromInputPayload(schemaId);
            updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, schemaId);
            final String finalTenant = tenant;
            SchemaModel schema = SchemaClientExceptionSupport.buildModel(jsonBody);
            SchemaClientExceptionSupport.tryRun(context, () -> {
                HttpResponse<SchemaModel> r = context.getSchemaClient().createAndGetInfo(
                    schema, Map.of("data-partition-id", finalTenant));
                context.setLastStatusCode(r.statusCode());
                context.setLastSchemaModel(r.body());
            });
            assertTrue(context.getLastStatusCode() == 200 || context.getLastStatusCode() == 201);
            prepareSchemaParameterMapList();
        }
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} and update versions")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_and_update_versions(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String baseSchemaId = "SchemaSanityTest:testSource:testEntity:"
            + currentMajorVersion + "." + currentMinorVersion + "." + currentPatchVersion;
        int nextPatchVersion = currentPatchVersion + 1;
        int nextMinorVersion = currentMinorVersion + 1;
        int nextMajorVersion = currentMajorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
            + nextMinorVersion + "." + nextPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        context.getSchemaRefMap().put("id1", schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion, schemaId);
        jsonBody = updateRefValue(jsonBody, baseSchemaId);
        String finalTenant = tenant;
        JsonElement finalJsonBody = jsonBody;
        SchemaModel schema = SchemaClientExceptionSupport.buildModel(finalJsonBody);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> r = context.getSchemaClient()
                .createAndGetInfo(schema, Map.of("data-partition-id", finalTenant));
            int code = r.statusCode();
            context.setLastStatusCode(code == 200 ? 201 : code);
            context.setLastSchemaModel(r.body());
        });
        prepareSchemaParameterMapList();
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string}")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int nextPatchVersion = currentPatchVersion + 1;
        int nextMinorVersion = currentMinorVersion + 1;
        int nextMajorVersion = currentMajorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
            + nextMinorVersion + "." + nextPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with no version increase")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_no_version_increase(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + ".0";
        context.setSchemaIdFromInputPayload(schemaId);
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} from directpayload and no version increase")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_from_directpayload_and_no_version_increase(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        String currentMajorVersion1 = jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
            .getAsJsonObject("schemaIdentity").get("schemaVersionMajor").toString();
        int currentMajorVersion = Integer.parseInt(currentMajorVersion1);
        String currentMinorVersion1 = jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
            .getAsJsonObject("schemaIdentity").get("schemaVersionMinor").toString();
        int currentMinorVersion = Integer.parseInt(currentMinorVersion1);
        String currentPatchVersion1 = jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
            .getAsJsonObject("schemaIdentity").get("schemaVersionPatch").toString();
        int currentPatchVersion = Integer.parseInt(currentPatchVersion1);
        String authority = jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
            .getAsJsonObject("schemaIdentity").get("authority").toString();
        String source = jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
            .getAsJsonObject("schemaIdentity").get("source").toString();
        String entityType = jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
            .getAsJsonObject("schemaIdentity").get("entityType").toString();
        String schemaId = authority + ":" + source + ":" + entityType + ":" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased patch version only")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_patch_version_only(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
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
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + currentPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased versions and update $ref1")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_versions_and_update_ref1(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String previousSchemaId = context.getSchemaIdFromInputPayload();
        int nextMajorVersion = currentMajorVersion + 1;
        int nextPatchVersion = currentPatchVersion + 1;
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion
            + "." + nextMinorVersion + "." + nextPatchVersion;
        context.getSchemaRefMap().put("id2", schemaId);
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion, schemaId);
        jsonBody = updateRefValue(jsonBody, previousSchemaId);
        postRequest(jsonBody, schemaId, tenant);
        prepareSchemaParameterMapList();
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased versions and update $ref2")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_versions_and_update_ref2(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String previousSchemaId = context.getSchemaIdFromInputPayload();
        int nextMajorVersion = currentMajorVersion + 1;
        int nextPatchVersion = currentPatchVersion + 1;
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion
            + "." + nextMinorVersion + "." + nextPatchVersion;
        context.getSchemaRefMap().put("id3", schemaId);
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion, schemaId);
        jsonBody = updateRefValue(jsonBody, previousSchemaId);
        postRequest(jsonBody, schemaId, tenant);
        prepareSchemaParameterMapList();
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version from any ID provided1")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_from_any_id_provided1(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String firstSchema = context.getSchemaRefMap().get("id1");
        String[] s = firstSchema.split(":");
        String[] s1 = s[3].split("\\.");
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(s1[1]);
        int currentMajorVersion = Integer.parseInt(s1[0]);
        int currentPatchVersion = Integer.parseInt(s1[2]);
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version from any ID provided2")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_from_any_id_provided2(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String firstSchema = context.getSchemaRefMap().get("id2");
        String[] s = firstSchema.split(":");
        String[] s1 = s[3].split("\\.");
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(s1[1]);
        int currentMajorVersion = Integer.parseInt(s1[0]);
        int currentPatchVersion = Integer.parseInt(s1[2]);
        String currentSchemaId = "SchemaSanityTest:testSource:testEntity:"
            + currentMajorVersion + "." + currentMajorVersion + "." + currentPatchVersion;
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
        jsonBody = updateRefValue(jsonBody, currentSchemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version from any ID provided3")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_from_any_id_provided3(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String firstSchema = context.getSchemaRefMap().get("id3");
        String[] s = firstSchema.split(":");
        String[] s1 = s[3].split("\\.");
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(s1[1]);
        int currentMajorVersion = Integer.parseInt(s1[0]);
        int currentPatchVersion = Integer.parseInt(s1[2]);
        String currentSchemaId = "SchemaSanityTest:testSource:testEntity:"
            + currentMajorVersion + "." + currentMajorVersion + "." + currentPatchVersion;
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
        jsonBody = updateRefValue(jsonBody, currentSchemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version with 2 count")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_with_2_count(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
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
        postRequest(jsonBody, schemaId, tenant);
        prepareSchemaParameterMapList();
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version with 1 count")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_with_1_count(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int nextMinorVersion = currentMinorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + nextMinorVersion + "." + currentPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
        prepareSchemaParameterMapList();
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with less minor version by 1 count than earlier")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_less_minor_version_by_1_count_than_earlier(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int previousMinorVersion = currentMinorVersion - 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + previousMinorVersion + "." + currentPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, previousMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with less minor version by 1 count than earlier and increase patch by 1")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_less_minor_version_by_1_count_than_earlier_and_increase_patch_by_1(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        int previousMinorVersion = currentMinorVersion - 1;
        int nextPatchVersion = currentPatchVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + previousMinorVersion + "." + nextPatchVersion;
        context.setSchemaIdFromInputPayload(schemaId);
        updatePatchVersionInJsonBody(jsonBody, previousMinorVersion, currentMajorVersion, nextPatchVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version and patch version")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_and_patch_version(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
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
        postRequest(jsonBody, schemaId, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version only")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_only(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int nextMinorVersion = currentMinorVersion + 2;
        int nextMajorVersion = currentMajorVersion + 1;
        String id = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "." + nextMinorVersion + ".0";
        updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, id);
        context.setSchemaIdFromInputPayload(id);
        context.setSchemaFromInputPayload(jsonBody.getAsJsonObject().get(TestConstants.SCHEMA).toString());
        context.setJsonPayloadForPostPUT(jsonBody.toString());
        postRequest(jsonBody, id, tenant);
    }

    @Given("I hit schema service POST API with {string} and data-partition-id as {string} without increasing any version")
    public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_without_increasing_any_version(String inputPayload, String tenant) throws IOException {
        String resolvedTenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        int currentPatchVersion = Integer.parseInt(context.getSchemaVersionPatch());
        String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + "." + currentPatchVersion;
        updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, currentPatchVersion, id);
        context.setSchemaIdFromInputPayload(id);
        context.setSchemaFromInputPayload(jsonBody.getAsJsonObject().get(TestConstants.SCHEMA).toString());
        context.setJsonPayloadForPostPUT(jsonBody.toString());
        postRequest(jsonBody, id, resolvedTenant);
    }

    @Then("service should respond back with {string} and {string}")
    public void service_should_respond_back_with_string_and_string(String responseStatusCode, String responseMessage) throws IOException {
        String body = TestFileUtil.readClasspathResource(responseMessage);
        JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        commonAssertion(jsonBody);
        Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
        Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
    }

    @Then("user gets response as {string} and {string}")
    public void user_gets_response_as_string_and_string(String responseStatusCode, String responseMessage) throws IOException {
        String body = TestFileUtil.readClasspathResource(responseMessage);
        JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        otherAssertion(jsonBody);
        Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
        Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
    }

    @Then("service should respond back with {string} and {string} and scope whould be {string}")
    public void service_should_respond_back_with_string_and_string_and_scope_whould_be_string(String responseStatusCode, String responseMessage, String scope) throws IOException {
        String body = TestFileUtil.readClasspathResource(responseMessage);
        JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        commonAssertion(jsonBody);
        Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
        Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
    }

    @Then("service should respond back with error {string} and {string}")
    public void service_should_respond_back_with_error_string_and_string(String responseStatusCode, String responseToBeVerified) throws IOException {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        if (context.getLastClientException() != null) {
            return;
        }
        String body = TestFileUtil.readClasspathResource(responseToBeVerified);
        Gson gsn = new Gson();
        JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
        String responseBody = context.getLastStringResponseBody();
        if (responseBody != null && !responseBody.isEmpty()) {
            JsonObject responseMsg = gsn.fromJson(responseBody, JsonObject.class);
            assertEquals(expectedData.toString(), responseMsg.toString());
        }
    }

    @Then("user gets patch version error response as {string} and {string}")
    public void user_gets_patch_version_error_response_as_string_and_string(String responseStatusCode, String responseToBeVerified) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        if (context.getLastClientException() != null) {
            String errorMsg = context.getLastClientException().getError() != null
                ? context.getLastClientException().getError().toString() : "";
            assertTrue(errorMsg.contains("Patch version validation failed."));
        }
    }

    @Then("user gets minor version error response as {string} and {string}")
    public void user_gets_minor_version_error_response_as_string_and_string(String responseStatusCode, String responseToBeVerified) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        if (context.getLastClientException() != null) {
            String errorMsg = context.getLastClientException().getError() != null
                ? context.getLastClientException().getError().toString() : "";
            assertTrue(errorMsg.contains("Minor version validation failed"));
        }
    }

    @Then("user gets oneOf attribute error response as {string} and {string}")
    public void user_gets_oneof_attribute_error_response_as_string_and_string(String responseStatusCode, String responseToBeVerified) {
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        if (context.getLastClientException() != null) {
            String errorMsg = context.getLastClientException().getError() != null
                ? context.getLastClientException().getError().toString() : "";
            assertTrue(errorMsg.contains("Changing list of") || errorMsg.contains("oneOf") || errorMsg.contains("allOf") || errorMsg.contains("anyOf"));
        }
    }

    @Given("I hit schema service POST API with {string} and auth token invalid")
    public void i_hit_schema_service_post_api_with_string_and_auth_token_invalid(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> response = context.getSchemaClient().post(
                new Gson().fromJson(jsonBody.toString(), SchemaModel.class),
                Map.of("Authorization", "Bearer invalid_token"));
            context.setLastStatusCode(response.statusCode());
            context.setLastSchemaModel(response.body());
        });
    }

    @Given("I hit schema service POST API with {string} for input json validation")
    public void i_hit_schema_service_post_api_with_string_for_input_json_validation(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> response = context.getSchemaClient().post(
                new Gson().fromJson(body, SchemaModel.class),
                Map.of("data-partition-id", context.getCurrentPartitionId() != null ? context.getCurrentPartitionId() : ""));
            context.setLastStatusCode(response.statusCode());
            context.setLastSchemaModel(response.body());
        });
    }

    @Given("I hit schema service POST API with {string}")
    public void i_hit_schema_service_post_api_with_string(String inputPayload) throws IOException {
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
        // Extract schemaId from payload
        JsonObject schemaIdentity = jsonBody.getAsJsonObject("schemaInfo").getAsJsonObject("schemaIdentity");
        String authority = schemaIdentity.get("authority").getAsString();
        String source = schemaIdentity.get("source").getAsString();
        String entityType = schemaIdentity.get("entityType").getAsString();
        String major = schemaIdentity.get("schemaVersionMajor").getAsString();
        String minor = schemaIdentity.get("schemaVersionMinor").getAsString();
        String patch = schemaIdentity.get("schemaVersionPatch").getAsString();
        String schemaId = authority + ":" + source + ":" + entityType + ":" + major + "." + minor + "." + patch;
        context.setSchemaIdFromInputPayload(schemaId);
        context.setSchemaFromInputPayload(jsonBody.get(TestConstants.SCHEMA).toString());
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> response = context.getSchemaClient().post(
                new Gson().fromJson(jsonBody.toString(), SchemaModel.class),
                Map.of("data-partition-id", context.getCurrentPartitionId() != null ? context.getCurrentPartitionId() : ""));
            context.setLastStatusCode(response.statusCode());
            context.setLastSchemaModel(response.body());
        });
    }

    @Given("I hit schema service POST API for supersededBy with {string} and data-partition-id as {string}")
    public void i_hit_schema_service_post_api_for_supersededby_with_string_and_datapartitionid_as_string(String inputPayload, String tenant) throws IOException {
        tenant = TenantResolver.resolveTenant(tenant);
        String body = TestFileUtil.readClasspathResource(inputPayload);
        JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
        int currentMinorVersion = Integer.parseInt(context.getSchemaVersionMinor());
        int currentMajorVersion = Integer.parseInt(context.getSchemaVersionMajor());
        String supersededById = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
            + currentMinorVersion + ".0";
        updateSupersededByInJsonBody(jsonBody, supersededById);
        context.setSupersededById(supersededById);
        int nextMinorVersion = currentMinorVersion + 1;
        int nextMajorVersion = currentMajorVersion + 1;
        String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
            + nextMinorVersion + ".0";
        context.setSchemaIdFromInputPayload(schemaId);
        updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, schemaId);
        postRequest(jsonBody, schemaId, tenant);
    }

    @Then("post service for supersededBy should respond back with {string} and {string}")
    public void post_service_for_supersededby_should_respond_back_with_string_and_string(String responseStatusCode, String responseMessage) throws IOException {
        String body = TestFileUtil.readClasspathResource(responseMessage);
        JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
        assertEquals(Integer.parseInt(responseStatusCode), context.getLastStatusCode());
        commonAssertion(jsonBody);
        SchemaModel model = context.getLastSchemaModel();
        Assert.assertNotNull(model.getSchemaInfo().getSchemaIdentity());
        assertEquals(context.getSchemaIdFromInputPayload(), model.getSchemaInfo().getSchemaIdentity().getId());
        // supersededBy check via Gson since SchemaInfo model doesn't have supersededBy field
        String rawJson = new Gson().toJson(model.getSchemaInfo());
        JsonObject schemaInfoJson = new Gson().fromJson(rawJson, JsonObject.class);
        Assert.assertNotNull(schemaInfoJson.get(TestConstants.SUPERSEDED_BY));
        JsonObject supersededByObj = schemaInfoJson.getAsJsonObject(TestConstants.SUPERSEDED_BY);
        assertEquals(context.getSupersededById(), supersededByObj.get(TestConstants.ID).getAsString());
    }

    private void postRequest(JsonElement jsonBody, String schemaId, String tenant) {
        context.setSchemaIdFromInputPayload(schemaId);
        context.setSchemaFromInputPayload(jsonBody.getAsJsonObject().get(TestConstants.SCHEMA).toString());
        context.setJsonPayloadForPostPUT(jsonBody.toString());
        SchemaModel schema = SchemaClientExceptionSupport.buildModel(jsonBody);
        SchemaClientExceptionSupport.tryRun(context, () -> {
            HttpResponse<SchemaModel> r = context.getSchemaClient().post(schema, Map.of("data-partition-id", tenant));
            context.setLastStatusCode(r.statusCode());
            context.setLastSchemaModel(r.body());
        });
    }

    private void commonAssertion(JsonObject expectedJsonBody) {
        SchemaModel model = context.getLastSchemaModel();
        SchemaIdentity identity = model.getSchemaInfo().getSchemaIdentity();
        assertEquals(getExpectedValue(expectedJsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.AUTHORITY),
            identity.getAuthority());
        assertEquals(getExpectedValue(expectedJsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.SOURCE),
            identity.getSource());
        assertEquals(getExpectedValue(expectedJsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.ENTITY),
            identity.getEntityType());
        Assert.assertNotNull(expectedJsonBody.get(TestConstants.DATE_CREATED));
        Assert.assertNotNull(expectedJsonBody.get(TestConstants.CREATED_BY));
    }

    private void otherAssertion(JsonObject expectedJsonBody) {
        commonAssertion(expectedJsonBody);
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

    private JsonElement updateRefValue(JsonElement jsonBody, String id) {
        JsonObject schema = jsonBody.getAsJsonObject().getAsJsonObject("schema");
        if (schema != null) {
            JsonObject properties = schema.getAsJsonObject("properties");
            if (properties != null) {
                JsonObject spatialLocation = properties.getAsJsonObject("SpatialLocation");
                if (spatialLocation != null) {
                    spatialLocation.remove("$ref");
                    spatialLocation.addProperty("$ref", "#/definitions/" + id);
                }
            }
        }
        String jsonBodyString = jsonBody.toString()
            .replace("SchemaSanityTest:testSource:testEntity:1.0.0", id);
        return new Gson().fromJson(jsonBodyString, JsonElement.class);
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

    private void updateSupersededByInJsonBody(JsonElement jsonBody, String id) {
        JsonElement supersededByBody = new Gson().fromJson(jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo")
            .getAsJsonObject(TestConstants.SCHEMA_IDENTITY).toString(), JsonElement.class);
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").add(TestConstants.SUPERSEDED_BY, supersededByBody);
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
            .remove(TestConstants.ID);
        jsonBody.getAsJsonObject().getAsJsonObject("schemaInfo").getAsJsonObject(TestConstants.SUPERSEDED_BY)
            .addProperty(TestConstants.ID, id);
    }

    public void prepareSchemaParameterMapList() {
        SchemaModel model = context.getLastSchemaModel();
        if (model != null && model.getSchemaInfo() != null && model.getSchemaInfo().getSchemaIdentity() != null) {
            SchemaIdentity identity = model.getSchemaInfo().getSchemaIdentity();
            context.setSchemaVersionMajor(identity.getSchemaVersionMajor());
            context.setSchemaVersionMinor(identity.getSchemaVersionMinor());
            context.setSchemaVersionPatch(identity.getSchemaVersionPatch());
        }
    }
}
