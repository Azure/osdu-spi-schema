package org.opengroup.osdu.schema.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.schema.constants.SchemaConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AuditEventsTest {

    private AuditEvents auditEvents;
    private List<String> resources;

    @BeforeEach
    public void setUp() {
        auditEvents = new AuditEvents("user@example.com", "127.0.0.1", "JUnitAgent", SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS);
        resources = Collections.singletonList("test-resource");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAuditLog(AuditPayload payload) {
        return (Map<String, Object>) payload.get("auditLog");
    }

    @Test
    public void should_createSchemaRegisteredEvent() {
        AuditPayload payload = auditEvents.getSchemaRegistered(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals(AuditEvents.SCHEMA_REGISTERED_ID, auditLog.get("actionId"));
        assertEquals(AuditAction.CREATE, auditLog.get("action"));
        assertEquals("user@example.com", auditLog.get("user"));
        assertEquals("127.0.0.1", auditLog.get("userIpAddress"));
        assertEquals("JUnitAgent", auditLog.get("userAgent"));
        assertEquals(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS, auditLog.get("userAuthorizedGroupName"));
        assertEquals(Arrays.asList(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS), auditLog.get("requiredGroupsForAction"));
        assertEquals("Schema registered  success", auditLog.get("message"));
    }

    @Test
    public void should_createSchemaRetrievedEvent() {
        AuditPayload payload = auditEvents.getSchemaRetrieved(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals(AuditEvents.SCHEMA_RETRIEVED_ID, auditLog.get("actionId"));
        assertEquals(AuditAction.READ, auditLog.get("action"));
        assertEquals(Arrays.asList(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_VIEWERS, SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS), auditLog.get("requiredGroupsForAction"));
    }

    @Test
    public void should_createSearchForSchemaEvent() {
        AuditPayload payload = auditEvents.getSearchForSchema(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals(AuditEvents.SEARCH_FOR_SCHEMA_ID, auditLog.get("actionId"));
        assertEquals(AuditAction.READ, auditLog.get("action"));
        assertEquals(Arrays.asList(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_VIEWERS), auditLog.get("requiredGroupsForAction"));
    }

    @Test
    public void should_createSchemaUpdatedEvent() {
        AuditPayload payload = auditEvents.getSchemaUpdated(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals(AuditEvents.SCHEMA_UPDATED_ID, auditLog.get("actionId"));
        assertEquals(AuditAction.UPDATE, auditLog.get("action"));
        assertEquals(Arrays.asList(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS), auditLog.get("requiredGroupsForAction"));
    }

    @Test
    public void should_createSystemSchemaRegisteredEvent() {
        AuditPayload payload = auditEvents.getSystemSchemaRegistered(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals(AuditEvents.SCHEMA_REGISTERED_ID, auditLog.get("actionId"));
        assertEquals(AuditAction.CREATE, auditLog.get("action"));
        assertEquals(Arrays.asList(SchemaConstants.WORKFLOW_SYSTEM_ADMIN, SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS), auditLog.get("requiredGroupsForAction"));
    }

    @Test
    public void should_createSystemSchemaUpdatedEvent() {
        AuditPayload payload = auditEvents.getSystemSchemaUpdated(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals(AuditEvents.SCHEMA_UPDATED_ID, auditLog.get("actionId"));
        assertEquals(AuditAction.UPDATE, auditLog.get("action"));
        assertEquals(Arrays.asList(SchemaConstants.WORKFLOW_SYSTEM_ADMIN, SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS), auditLog.get("requiredGroupsForAction"));
    }

    @Test
    public void should_createFailureEvent() {
        AuditPayload payload = auditEvents.getSchemaRegistered(AuditStatus.FAILURE, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals(AuditStatus.FAILURE, auditLog.get("status"));
        assertEquals("Schema registered  failure", auditLog.get("message"));
    }

    @Test
    public void should_allowNullUserAuthorizedGroupName() {
        AuditEvents events = new AuditEvents("user@example.com", "127.0.0.1", "agent", null);
        AuditPayload payload = events.getSchemaRegistered(AuditStatus.SUCCESS, resources);
        assertNotNull(payload);
        Map<String, Object> auditLog = getAuditLog(payload);
        assertEquals("unknown", auditLog.get("userAuthorizedGroupName"));
    }
}
