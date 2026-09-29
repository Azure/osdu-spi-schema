package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.HttpClientFactory;
import org.opengroup.osdu.schema.util.JsonUtils;

import com.google.common.collect.Maps;
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

	static String[] GetListBaseFilterArray;
	static String[] GetListVersionFilterArray;
	String queryParameter;
	static HashMap<String, String> map1=new HashMap<>();

	List<HashMap<String, String>> list_schemaParameterMap = new ArrayList<HashMap<String, String>>();

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} only if status is not development")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_only_if_status_is_not_development(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String resp = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		JsonObject schemaInfosList = gsn.fromJson(resp, JsonObject.class);
		JsonArray root = (JsonArray) schemaInfosList.get("schemaInfos");
		if (!"DEVELOPMENT".equals(context.getStatus()) || (root.size() == 0)) {

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
			int nextPatchVersion = currentPatchVersion + 1;
			String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
		+ nextMinorVersion + "." + nextPatchVersion;

			this.context.setSchemaIdFromInputPayload(schemaId);

			updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, schemaId);
			HttpResponse response = postRequest(jsonBody, schemaId, tenant);

			assertEquals("201", String.valueOf(response.getCode()));
			this.context.setHttpResponse(response);
			prepareSchemaParameterMapList();
		}
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} and update versions")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_and_update_versions(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int nextPatchVersion = currentPatchVersion + 1;
		int nextMinorVersion = currentMinorVersion + 1;
		int nextMajorVersion = currentMajorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
	+ nextMinorVersion + "." + nextPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		map1.put("id1", schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion,
	schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
		prepareSchemaParameterMapList();
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string}")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int nextPatchVersion = currentPatchVersion + 1;
		int nextMinorVersion = currentMinorVersion + 1;
		int nextMajorVersion = currentMajorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
	+ nextMinorVersion + "." + nextPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion,
	schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with no version increase")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_no_version_increase(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + ".0";
		this.context.setSchemaIdFromInputPayload(schemaId);
		updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} from directpayload and no version increase")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_from_directpayload_and_no_version_increase(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
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


		this.context.setSchemaIdFromInputPayload(schemaId);
		updateVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion, schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased patch version only")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_patch_version_only(String inputPayload, String tenant) throws Exception {
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
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version(String inputPayload, String tenant) throws Exception {
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
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion,
	schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased versions and update $ref1")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_versions_and_update_ref1(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String previousSchemaId = this.context.getSchemaIdFromInputPayload();
		System.out.println("previousSchemaId : " + previousSchemaId);
		int nextMajorVersion = currentMajorVersion + 1;
		int nextPatchVersion = currentPatchVersion + 1;
		int nextMinorVersion = currentMinorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion
	+ "." + nextMinorVersion + "." + nextPatchVersion;
		map1.put("id2", schemaId);
		System.out.println("id2 : " + schemaId);
		//idVals[1] = schemaId;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion, schemaId);
		jsonBody = updateRefValue(jsonBody, previousSchemaId);
		System.out.println("json body is : " + jsonBody.toString());
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
		prepareSchemaParameterMapList();
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased versions and update $ref2")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_versions_and_update_ref2(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String previousSchemaId = this.context.getSchemaIdFromInputPayload();
		System.out.println("previousSchemaId : " + previousSchemaId);
		int nextMajorVersion = currentMajorVersion + 1;
		int nextPatchVersion = currentPatchVersion + 1;
		int nextMinorVersion = currentMinorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion
	+ "." + nextMinorVersion + "." + nextPatchVersion;
		map1.put("id3", schemaId);
		//idVals[2] = schemaId;
		System.out.println("id3 : " + schemaId);
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, nextPatchVersion, schemaId);
		jsonBody = updateRefValue(jsonBody, previousSchemaId);
		System.out.println("json body is : " + jsonBody.toString());
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
		prepareSchemaParameterMapList();
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version from any ID provided1")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_from_any_id_provided1(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);

		String firstSchema = map1.get("id1");
		System.out.println("firstSchema : " + firstSchema);
		String[] s = firstSchema.split(":");
		System.out.println("s[3] : " + s[3]);
		String[] s1 = s[3].split("\\.");
		System.out.println("s1[0] : " + s1[0]);
		System.out.println("s1[1] : " + s1[1]);
		System.out.println("s1[2] : " + s1[2]);

		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(s1[1]);
		int currentMajorVersion = Integer.parseInt(s1[0]);
		int currentPatchVersion = Integer.parseInt(s1[2]);
		String currentSchemaId = "SchemaSanityTest:testSource:testEntity:"
	+ currentMajorVersion + "." + currentMajorVersion + "." + currentPatchVersion;
		int nextMinorVersion = currentMinorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ nextMinorVersion + "." + currentPatchVersion;
		System.out.println("Current schemaId : " + schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
		System.out.println("json body is : " + jsonBody.toString());
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version from any ID provided2")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_from_any_id_provided2(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);

		String firstSchema = map1.get("id2");
		System.out.println("firstSchema : " + firstSchema);
		String[] s = firstSchema.split(":");
		System.out.println("s[3] : " + s[3]);
		String[] s1 = s[3].split("\\.");
		System.out.println("s1[0] : " + s1[0]);
		System.out.println("s1[1] : " + s1[1]);
		System.out.println("s1[2] : " + s1[2]);

		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(s1[1]);
		int currentMajorVersion = Integer.parseInt(s1[0]);
		int currentPatchVersion = Integer.parseInt(s1[2]);
		String currentSchemaId = "SchemaSanityTest:testSource:testEntity:"
	+ currentMajorVersion + "." + currentMajorVersion + "." + currentPatchVersion;
		int nextMinorVersion = currentMinorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ nextMinorVersion + "." + currentPatchVersion;
		System.out.println("Current schemaId : " + schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
		jsonBody = updateRefValue(jsonBody, currentSchemaId);
		System.out.println("json body is : " + jsonBody.toString());
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version from any ID provided3")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_from_any_id_provided3(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);

		String firstSchema = map1.get("id3");
		System.out.println("firstSchema : " + firstSchema);
		String[] s = firstSchema.split(":");
		System.out.println("s[3] : " + s[3]);
		String[] s1 = s[3].split("\\.");
		System.out.println("s1[0] : " + s1[0]);
		System.out.println("s1[1] : " + s1[1]);
		System.out.println("s1[2] : " + s1[2]);

		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(s1[1]);
		int currentMajorVersion = Integer.parseInt(s1[0]);
		int currentPatchVersion = Integer.parseInt(s1[2]);
		String currentSchemaId = "SchemaSanityTest:testSource:testEntity:"
	+ currentMajorVersion + "." + currentMajorVersion + "." + currentPatchVersion;
		int nextMinorVersion = currentMinorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ nextMinorVersion + "." + currentPatchVersion;
		System.out.println("Current schemaId : " + schemaId);
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion, schemaId);
		jsonBody = updateRefValue(jsonBody, currentSchemaId);
		System.out.println("json body is : " + jsonBody.toString());
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version with 2 count")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_with_2_count(String inputPayload, String tenant) throws Exception {
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
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
		prepareSchemaParameterMapList();
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version with 1 count")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_with_1_count(String inputPayload, String tenant) throws Exception {
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
		updatePatchVersionInJsonBody(jsonBody, nextMinorVersion, currentMajorVersion, currentPatchVersion,
	schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
		prepareSchemaParameterMapList();
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with less minor version by 1 count than earlier")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_less_minor_version_by_1_count_than_earlier(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int previousMinorVersion = currentMinorVersion - 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ previousMinorVersion + "." + currentPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, previousMinorVersion, currentMajorVersion,
	currentPatchVersion, schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with less minor version by 1 count than earlier and increase patch by 1")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_less_minor_version_by_1_count_than_earlier_and_increase_patch_by_1(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		int previousMinorVersion = currentMinorVersion - 1;
		int nextPatchVersion = currentPatchVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ previousMinorVersion + "." + nextPatchVersion;
		this.context.setSchemaIdFromInputPayload(schemaId);
		updatePatchVersionInJsonBody(jsonBody, previousMinorVersion, currentMajorVersion, nextPatchVersion,
	schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version and patch version")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_and_patch_version(String inputPayload, String tenant) throws Exception {
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
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} with increased minor version only")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_with_increased_minor_version_only(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int nextMinorVersion = currentMinorVersion + 2;
		int nextMajorVersion = currentMajorVersion + 1;
		String id = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "." + nextMinorVersion
	+ ".0";
		updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, id);
		body = new Gson().toJson(jsonBody);
		this.context.setSchemaIdFromInputPayload(id);
		this.context
	.setSchemaFromInputPayload(jsonBody.getAsJsonObject().get(TestConstants.SCHEMA).toString());
		this.context.setJsonPayloadForPostPUT(jsonBody.toString());
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		this.context.setAuthHeaders(headers);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.POST_ENDPOINT).body(jsonBody.toString())
	.httpMethod(HttpRequest.POST).requestHeaders(headers).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} and data-partition-id as {string} without increasing any version")
	public void i_hit_schema_service_post_api_with_string_and_datapartitionid_as_string_without_increasing_any_version(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);
		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());
		int currentPatchVersion = Integer.parseInt(this.context.getSchemaVersionPatch());
		String id = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + "." + currentPatchVersion;
		updatePatchVersionInJsonBody(jsonBody, currentMinorVersion, currentMajorVersion,
	currentPatchVersion, id);
		body = new Gson().toJson(jsonBody);
		this.context.setSchemaIdFromInputPayload(id);
		this.context
	.setSchemaFromInputPayload(jsonBody.getAsJsonObject().get(TestConstants.SCHEMA).toString());
		this.context.setJsonPayloadForPostPUT(jsonBody.toString());
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		this.context.setAuthHeaders(headers);
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.POST_ENDPOINT).body(jsonBody.toString())
	.httpMethod(HttpRequest.POST).requestHeaders(headers).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("service should respond back with {string} and {string}")
	public void service_should_respond_back_with_string_and_string(String ReponseStatusCode, String ResponseMessage) throws Exception {
		String body = this.context.getFileUtils().read(ResponseMessage);
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
			commonAssertion(response, jsonBody);
			Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
			Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
		}
	}

	@Then("user gets response as {string} and {string}")
	public void user_gets_response_as_string_and_string(String ReponseStatusCode, String ResponseMessage) throws Exception {
		String body = this.context.getFileUtils().read(ResponseMessage);
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
			otherAssertion(response, jsonBody);
			Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
			Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
		}
	}

	@Then("service should respond back with {string} and {string} and scope whould be {string}")
	public void service_should_respond_back_with_string_and_string_and_scope_whould_be_string(String ReponseStatusCode, String ResponseMessage, String scope) throws Exception {
		String body = this.context.getFileUtils().read(ResponseMessage);
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));

			commonAssertion(response, jsonBody);

			Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
			Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
		}
	}

	@Then("service should respond back with error {string} and {string}")
	public void service_should_respond_back_with_error_string_and_string(String ReponseStatusCode, String ResponseToBeVerified) throws Exception {
		HttpResponse response = this.context.getHttpResponse();
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
		String body = this.context.getFileUtils().read(ResponseToBeVerified);
		Gson gsn = new Gson();
		JsonObject expectedData = gsn.fromJson(body, JsonObject.class);
		JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
		if (!response.getBody().isEmpty())
			assertEquals(expectedData.toString(), responseMsg.toString());
	}

	@Then("user gets patch version error response as {string} and {string}")
	public void user_gets_patch_version_error_response_as_string_and_string(String ReponseStatusCode, String ResponseToBeVerified) throws IOException {
		HttpResponse response = this.context.getHttpResponse();
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
		String body = this.context.getFileUtils().read(ResponseToBeVerified);
		Gson gsn = new Gson();
		JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
		if (!response.getBody().isEmpty())
			assertTrue(responseMsg.toString().contains("Patch version validation failed."));
	}

	@Then("user gets minor version error response as {string} and {string}")
	public void user_gets_minor_version_error_response_as_string_and_string(String ReponseStatusCode, String ResponseToBeVerified) throws IOException {
		HttpResponse response = this.context.getHttpResponse();
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
		String body = this.context.getFileUtils().read(ResponseToBeVerified);
		Gson gsn = new Gson();
		JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
		if (!response.getBody().isEmpty())
			assertTrue(responseMsg.toString().contains("Minor version validation failed"));
	}

	@Then("user gets oneOf attribute error response as {string} and {string}")
	public void user_gets_oneof_attribute_error_response_as_string_and_string(String ReponseStatusCode, String ResponseToBeVerified) throws IOException {
		HttpResponse response = this.context.getHttpResponse();
		assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));
		String body = this.context.getFileUtils().read(ResponseToBeVerified);
		Gson gsn = new Gson();
		JsonObject responseMsg = gsn.fromJson(response.getBody().toString(), JsonObject.class);
		if (!response.getBody().isEmpty())
			assertTrue(responseMsg.toString().contains(
		"Changing list of \\\"oneOf\\\",\\\"allOf\\\" or \\\"anyOf\\\"  is not permitted,"));
	}

	@Given("I hit schema service POST API with {string} and auth token invalid")
	public void i_hit_schema_service_post_api_with_string_and_auth_token_invalid(String inputPayload) throws Exception {
		String body = this.context.getFileUtils().read(inputPayload);
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
		Map<String, String> invalidAuthTokenHeaders = this.context.getAuthHeaders();
		invalidAuthTokenHeaders.put(TestConstants.AUTHORIZATION, this.context.getToken() + "_invalidHeaders");
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.POST_ENDPOINT)
	.body(jsonBody.toString()).httpMethod(HttpRequest.POST).requestHeaders(invalidAuthTokenHeaders)
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string} for input json validation")
	public void i_hit_schema_service_post_api_with_string_for_input_json_validation(String inputPayload) throws IOException {
		String body = this.context.getFileUtils().read(inputPayload);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.POST_ENDPOINT)
	.body(body).httpMethod(HttpRequest.POST).requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API with {string}")
	public void i_hit_schema_service_post_api_with_string(String inputPayload) throws Exception {
		String body = this.context.getFileUtils().read(inputPayload);
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
		this.context.setSchemaIdFromInputPayload(
	JsonUtils.getAsJsonPath(jsonBody.toString()).get(TestConstants.schemaIdOfInputPayload));
		this.context.setSchemaFromInputPayload(jsonBody.get(TestConstants.SCHEMA).toString());
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.POST_ENDPOINT)
	.body(jsonBody.toString()).httpMethod(HttpRequest.POST)
	.requestHeaders(this.context.getAuthHeaders()).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Given("I hit schema service POST API for supersededBy with {string} and data-partition-id as {string}")
	public void i_hit_schema_service_post_api_for_supersededby_with_string_and_datapartitionid_as_string(String inputPayload, String tenant) throws Exception {
		tenant = selectTenant(tenant);
		String body = this.context.getFileUtils().read(inputPayload);

		JsonElement jsonBody = new Gson().fromJson(body, JsonElement.class);
		int currentMinorVersion = Integer.parseInt(this.context.getSchemaVersionMinor());
		int currentMajorVersion = Integer.parseInt(this.context.getSchemaVersionMajor());

		String supersededById = "SchemaSanityTest:testSource:testEntity:" + currentMajorVersion + "."
	+ currentMinorVersion + ".0";
		updateSupersededByInJsonBody(jsonBody, supersededById);
		this.context.setSupersededById(supersededById);

		int nextMinorVersion = currentMinorVersion + 1;
		int nextMajorVersion = currentMajorVersion + 1;
		String schemaId = "SchemaSanityTest:testSource:testEntity:" + nextMajorVersion + "."
	+ nextMinorVersion + ".0";

		this.context.setSchemaIdFromInputPayload(schemaId);

		updateVersionInJsonBody(jsonBody, nextMinorVersion, nextMajorVersion, schemaId);
		HttpResponse response = postRequest(jsonBody, schemaId, tenant);
		this.context.setHttpResponse(response);
	}

	@Then("post service for supersededBy should respond back with {string} and {string}")
	public void post_service_for_supersededby_should_respond_back_with_string_and_string(String ReponseStatusCode, String ResponseMessage) throws Exception {
		String body = this.context.getFileUtils().read(ResponseMessage);
		JsonObject jsonBody = new Gson().fromJson(body, JsonObject.class);
		HttpResponse response = this.context.getHttpResponse();
		if (response != null) {
			assertEquals(ReponseStatusCode, String.valueOf(response.getCode()));

			commonAssertion(response, jsonBody);

			Assert.assertNotNull(getResponseValue(TestConstants.SUPERSEDED_BY));

			assertEquals(
		getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.ID),
		this.context.getSchemaIdFromInputPayload());

			assertEquals(
		getResponseValue(TestConstants.SUPERSEDED_BY + TestConstants.DOT + TestConstants.ID),
		this.context.getSupersededById());

		}
	}

	private HttpResponse postRequest(JsonElement jsonBody, String schemaId, String tenant) {
		this.context.setSchemaIdFromInputPayload(schemaId);
		this.context.setSchemaFromInputPayload(jsonBody.getAsJsonObject().get(TestConstants.SCHEMA).toString());
		this.context.setJsonPayloadForPostPUT(jsonBody.toString());
		Map<String, String> headers = this.context.getAuthHeaders();
		headers.put(TestConstants.DATA_PARTITION_ID, tenant);
		this.context.setAuthHeaders(headers);
		HttpRequest httpRequest = HttpRequest.builder().url(TestConstants.HOST + TestConstants.POST_ENDPOINT)
				.body(jsonBody.toString()).httpMethod(HttpRequest.POST).requestHeaders(headers).build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		return response;
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

	private void otherAssertion(HttpResponse response, JsonObject jsonBody) {

		assertEquals(getExpectedValue(jsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.AUTHORITY),
				getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.AUTHORITY));

		assertEquals(getExpectedValue(jsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.SOURCE),
				getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.SOURCE));

		assertEquals(getExpectedValue(jsonBody, TestConstants.SCHEMA_IDENTITY, TestConstants.ENTITY),
				getResponseValue(TestConstants.SCHEMA_IDENTITY + TestConstants.DOT + TestConstants.ENTITY));

		Assert.assertNotNull(jsonBody.get(TestConstants.DATE_CREATED));
		Assert.assertNotNull(jsonBody.get(TestConstants.CREATED_BY));
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
		jsonBody.getAsJsonObject().getAsJsonObject("schema").getAsJsonObject("properties")
				.getAsJsonObject("SpatialLocation").getAsJsonObject().remove("$ref");
		jsonBody.getAsJsonObject().getAsJsonObject("schema").getAsJsonObject("properties")
				.getAsJsonObject("SpatialLocation").getAsJsonObject().addProperty("$ref", "#/definitions/" + id);
		System.out.println("Final value of ref is : " + jsonBody.getAsJsonObject().getAsJsonObject("schema")
				.getAsJsonObject("properties").getAsJsonObject("SpatialLocation").get("$ref"));
		
		String jsonBodyString = jsonBody.toString().replace("SchemaSanityTest:testSource:testEntity:1.0.0", id);
		System.out.println("============ Final string payload body ================" + jsonBodyString);
		Gson gson = new Gson();
		jsonBody = gson.fromJson(jsonBodyString, JsonElement.class);
		System.out.println("============ Final json payload body ================" + jsonBody);
		return jsonBody;
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
		String response = this.context.getHttpResponse().getBody();
		Gson gsn = new Gson();
		JsonObject root = gsn.fromJson(response, JsonObject.class);
		JsonObject schemaIdentity_ForEachSchemaIdentity = (JsonObject) root.get("schemaIdentity");
		this.context
				.setSchemaVersionMajor(schemaIdentity_ForEachSchemaIdentity!=null && schemaIdentity_ForEachSchemaIdentity.get("schemaVersionMajor").getAsString()!=null?schemaIdentity_ForEachSchemaIdentity.get("schemaVersionMajor").getAsString():null);
		this.context
				.setSchemaVersionMinor(schemaIdentity_ForEachSchemaIdentity!=null && schemaIdentity_ForEachSchemaIdentity.get("schemaVersionMinor").getAsString()!=null?schemaIdentity_ForEachSchemaIdentity.get("schemaVersionMinor").getAsString():null);
		this.context
				.setSchemaVersionPatch(schemaIdentity_ForEachSchemaIdentity!=null && schemaIdentity_ForEachSchemaIdentity.get("schemaVersionPatch").getAsString()!=null?schemaIdentity_ForEachSchemaIdentity.get("schemaVersionPatch").getAsString():null);
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