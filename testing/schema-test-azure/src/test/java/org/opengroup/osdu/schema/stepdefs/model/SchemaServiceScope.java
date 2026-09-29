// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
