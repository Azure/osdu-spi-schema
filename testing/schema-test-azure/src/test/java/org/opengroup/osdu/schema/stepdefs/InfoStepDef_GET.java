package org.opengroup.osdu.schema.stepdefs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.inject.Inject;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;
import org.opengroup.osdu.schema.util.HttpClientFactory;
import org.opengroup.osdu.schema.util.VersionInfoUtils;

public class InfoStepDef_GET {

    @Inject
    private SchemaServiceScope context;

    private VersionInfoUtils versionInfoUtil = new VersionInfoUtils();


    @Given("I send get request to version info endpoint")
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
}
