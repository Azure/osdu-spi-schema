/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2021-2023 EPAM Systems, Inc
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

import com.google.inject.Inject;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.HttpClientFactory;

import static org.junit.Assert.assertEquals;

public class HealthApiStepDef_GET {

  @Inject private SchemaServiceScope context;

  @Given("I send get request without a token to liveness check endpoint")
  public void i_send_get_request_to_liveness_check_endpoint() {
    HttpRequest httpRequest =
        HttpRequest.builder()
            .url(TestConstants.HOST + TestConstants.GET_LIVENESS_ENDPOINT)
            .httpMethod(HttpRequest.GET)
            .build();
    HttpResponse response = HttpClientFactory.getInstance().send(httpRequest);
    this.context.setHttpResponse(response);
  }

  @Then("service should respond back with 200 in response")
  public void service_should_respond_back_with_200_in_response() {
    assertEquals(200, this.context.getHttpResponse().getCode());
  }
}
