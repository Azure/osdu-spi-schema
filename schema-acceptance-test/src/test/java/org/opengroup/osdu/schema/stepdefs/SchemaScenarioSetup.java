package org.opengroup.osdu.schema.stepdefs;

import com.google.inject.Inject;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.opengroup.osdu.core.test.auth.UserType;
import org.opengroup.osdu.core.test.client.RetryConfiguration;
import org.opengroup.osdu.core.test.client.SchemaClient;
import org.opengroup.osdu.core.test.config.TestInitializer;
import org.opengroup.osdu.core.test.service.ServiceType;
import org.opengroup.osdu.schema.stepdefs.model.SchemaServiceScope;

import java.util.List;

public class SchemaScenarioSetup {

    private static final List<UserType> USER_TYPES = List.of(UserType.PRIVILEGED_USER);
    private static final List<ServiceType> SERVICE_TYPES = List.of(
        ServiceType.SCHEMA_V1,
        ServiceType.INDEXER_V2,
        ServiceType.ENTITLEMENTS_V2
    );

    @Inject
    private SchemaServiceScope context;

    @Before
    public void setUp() {
        TestInitializer initializer = TestInitializer.getSharedTestInitializer(
            USER_TYPES, SERVICE_TYPES, RetryConfiguration.none());
        context.setSchemaClient(new SchemaClient(
            initializer.getServicesConfig(), initializer.getTokenProvider(),
            UserType.PRIVILEGED_USER));
    }

    @After
    public void tearDown() {
        if (context.getSchemaClient() != null) {
            context.getSchemaClient().teardown();
        }
    }
}
