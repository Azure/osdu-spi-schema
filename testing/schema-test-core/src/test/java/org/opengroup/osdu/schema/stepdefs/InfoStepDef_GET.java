/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.inject.Inject;
import org.opengroup.osdu.schema.constants.TestConstants;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.AuthUtil;
import org.opengroup.osdu.schema.util.HttpClientFactory;
import org.opengroup.osdu.schema.util.VersionInfoUtils;

import java.util.HashMap;
import java.util.Map;

public class InfoStepDef_GET {

  @Inject
  private SchemaServiceScope context;

  private VersionInfoUtils versionInfoUtil = new VersionInfoUtils();

	@Given("I send get request without a token to version info endpoint")
	public void i_send_get_request_to_version_info_endpoint() {
		HttpRequest httpRequest = HttpRequest.builder()
	.url(TestConstants.HOST + TestConstants.GET_INFO_ENDPOINT)
	.httpMethod(HttpRequest.GET)
	.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("service should respond back with version info in response")
	public void service_should_respond_back_with_version_info_in_response() {
		assertEquals(200, this.context.getHttpResponse().getCode());

		VersionInfoUtils.VersionInfo responseObject = this.versionInfoUtil
	.getVersionInfoFromResponse(this.context.getHttpResponse());

		assertNotNull(responseObject.groupId);
		assertNotNull(responseObject.artifactId);
		assertNotNull(responseObject.version);
		assertNotNull(responseObject.buildTime);
		assertNotNull(responseObject.branch);
		assertNotNull(responseObject.commitId);
		assertNotNull(responseObject.commitMessage);
	}
	@Given("I send get request to version info with Trailing Slash")
	public void i_send_get_request_to_version_info_withTrailingSlash() throws Exception {
		if (this.context.getToken() == null) {
			String token = new AuthUtil().getToken();
			this.context.setToken(token);
		}
		Map<String, String> authHeaders = new HashMap<String, String>();
		authHeaders.put(TestConstants.AUTHORIZATION, this.context.getToken());
		HttpRequest httpRequest = HttpRequest.builder()
				.url(TestConstants.HOST + TestConstants.GET_INFO_ENDPOINT+"/")
				.httpMethod(HttpRequest.GET)
				.requestHeaders(authHeaders)
				.build();
		HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
		this.context.setHttpResponse(response);
	}

	@Then("service should respond back with trailing slash")
	public void should_returnInfo_withTrailingSlash() {
		assertEquals(200, this.context.getHttpResponse().getCode());

		VersionInfoUtils.VersionInfo responseObject = this.versionInfoUtil
				.getVersionInfoFromResponse(this.context.getHttpResponse());

		assertNotNull(responseObject.groupId);
		assertNotNull(responseObject.artifactId);
		assertNotNull(responseObject.version);
		assertNotNull(responseObject.buildTime);
		assertNotNull(responseObject.branch);
		assertNotNull(responseObject.commitId);
		assertNotNull(responseObject.commitMessage);
	}


}
