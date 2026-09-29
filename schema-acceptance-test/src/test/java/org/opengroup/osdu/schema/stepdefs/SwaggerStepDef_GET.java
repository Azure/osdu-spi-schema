/*
 * Copyright 2020-2024 Google LLC
 * Copyright 2020-2024 EPAM Systems, Inc
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

import com.google.inject.Inject;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.SchemaClientExceptionSupport;

public class SwaggerStepDef_GET {

  @Inject
  private SchemaServiceScope context;

  @Given("I send get request without a token to swagger ui endpoint")
  public void i_send_get_request_to_swagger_ui_endpoint() {
    SchemaClientExceptionSupport.tryRun(context, () -> {
      int statusCode = context.getSchemaClient().getSwagger().statusCode();
      context.setLastStatusCode(statusCode);
    });
  }

  @Then("service should respond back with 200 for swagger ui")
  public void service_should_respond_back_with_200_for_swagger_ui() {
    assertEquals(200, context.getLastStatusCode());
  }

  @Given("I send get request without a token to swagger api docs endpoint")
  public void i_send_get_request_to_swagger_api_docs_endpoint() {
    SchemaClientExceptionSupport.tryRun(context, () -> {
      int statusCode = context.getSchemaClient().getApiDocs().statusCode();
      context.setLastStatusCode(statusCode);
    });
  }

  @Then("service should respond back with 200 for swagger api docs")
  public void service_should_respond_back_with_200_for_swagger_api_docs() {
    assertEquals(200, context.getLastStatusCode());
  }
}
