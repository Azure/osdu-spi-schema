package org.opengroup.osdu.schema.logging;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.schema.constants.SchemaConstants;

import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AuditOperationTest {

    @Test
    public void should_haveCorrectRolesForSchemaRegistered() {
        List<String> roles = AuditOperation.SCHEMA_REGISTERED.getRequiredGroups();
        assertEquals(1, roles.size());
        assertTrue(roles.containsAll(Collections.singletonList(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS)));
    }

    @Test
    public void should_haveCorrectRolesForSchemaRetrieved() {
        List<String> roles = AuditOperation.SCHEMA_RETRIEVED.getRequiredGroups();
        assertEquals(2, roles.size());
        assertTrue(roles.contains(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_VIEWERS));
        assertTrue(roles.contains(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS));
    }

    @Test
    public void should_haveCorrectRolesForSearchForSchema() {
        List<String> roles = AuditOperation.SEARCH_FOR_SCHEMA.getRequiredGroups();
        assertEquals(1, roles.size());
        assertTrue(roles.containsAll(Collections.singletonList(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_VIEWERS)));
    }

    @Test
    public void should_haveCorrectRolesForSchemaUpdated() {
        List<String> roles = AuditOperation.SCHEMA_UPDATED.getRequiredGroups();
        assertEquals(1, roles.size());
        assertTrue(roles.containsAll(Collections.singletonList(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS)));
    }

    @Test
    public void should_haveCorrectRolesForSystemSchemaRegistered() {
        List<String> roles = AuditOperation.SYSTEM_SCHEMA_REGISTERED.getRequiredGroups();
        assertEquals(2, roles.size());
        assertTrue(roles.contains(SchemaConstants.WORKFLOW_SYSTEM_ADMIN));
        assertTrue(roles.contains(SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS));
    }

    @Test
    public void should_haveCorrectRolesForSystemSchemaUpdated() {
        List<String> roles = AuditOperation.SYSTEM_SCHEMA_UPDATED.getRequiredGroups();
        assertEquals(2, roles.size());
        assertTrue(roles.contains(SchemaConstants.WORKFLOW_SYSTEM_ADMIN));
        assertTrue(roles.contains(SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS));
    }

    @Test
    public void should_returnUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> AuditOperation.SCHEMA_REGISTERED.getRequiredGroups().add("should-fail"));
    }

    @Test
    public void should_haveAllOperationsDefined() {
        assertEquals(6, AuditOperation.values().length);
    }
}
