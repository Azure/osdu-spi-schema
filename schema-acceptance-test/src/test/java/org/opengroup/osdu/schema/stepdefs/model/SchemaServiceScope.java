package org.opengroup.osdu.schema.stepdefs.model;

import com.google.inject.Inject;
import io.cucumber.guice.ScenarioScoped;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.client.SchemaClient;
import org.opengroup.osdu.core.test.client.model.schema.SchemaModel;

import java.util.HashMap;
import java.util.Map;

@ScenarioScoped
@Getter
@Setter
public class SchemaServiceScope {

    private String schemaVersionMinor = "0";
    private String schemaVersionMajor = "0";
    private String schemaVersionPatch = "0";

    private String jsonPayloadForPostPUT;
    private String status;
    private String scope;
    private Map<String, String> queryParams;
    private String schemaIdFromInputPayload;
    private String schemaFromInputPayload;
    private String supersededById;

    private SchemaClient schemaClient;
    private String currentPartitionId;
    private int lastStatusCode;
    private SchemaModel lastSchemaModel;
    private String lastStringResponseBody;
    private ClientException lastClientException;
    private Map<String, String> schemaRefMap = new HashMap<>();
}
