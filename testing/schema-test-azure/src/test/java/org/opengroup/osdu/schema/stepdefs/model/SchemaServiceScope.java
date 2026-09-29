package org.opengroup.osdu.schema.stepdefs.model;

import java.util.Map;

import org.opengroup.osdu.schema.util.FileUtils;

import com.google.inject.Inject;

import io.cucumber.guice.ScenarioScoped;

import lombok.Data;

@ScenarioScoped
@Data
public class SchemaServiceScope {

	@Inject
	private FileUtils fileUtils;

	private String token;
	private String jobId;
	private String schemaVersionMinor;
	private String schemaVersionMajor;
	private String schemaVersionPatch;
	private HttpResponse httpResponse;
	private String jsonPayloadForPostPUT;
	private String status;

	private Map<String, String> authHeaders;
	private Map<String, String> queryParams;
	private String SchemaIdFromInputPayload;
	private String SchemaFromInputPayload;
	private String supersededById;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setJsonPayloadForPostPUT(String jsonPayloadForPostPUT) {
		this.jsonPayloadForPostPUT = jsonPayloadForPostPUT;
	}

	public String getJsonPayloadForPostPUT() {
		return jsonPayloadForPostPUT;
	}

	public String getSchemaVersionMinor() {
		return schemaVersionMinor;
	}

	public void setSchemaIdFromInputPayload(String schemaId) {
		this.SchemaIdFromInputPayload = schemaId;
	}

	public void setSchemaVersionMinor(String schemaVersionMinor) {
		this.schemaVersionMinor = schemaVersionMinor;
	}

	public String getSchemaVersionMajor() {
		return schemaVersionMajor;
	}
	

	public void getSchemaVersionMajor(String schemaVersionMajor) {
		this.schemaVersionMajor = schemaVersionMajor;
	}
	
	public String getSchemaVersionPatch() {
		return schemaVersionPatch;
	}
	

	public void getSchemaVersionPatch(String schemaVersionPatch) {
		this.schemaVersionPatch = schemaVersionPatch;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


}
