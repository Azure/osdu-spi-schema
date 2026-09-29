package org.opengroup.osdu.schema.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.constants.SchemaConstants;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

public class AuditLoggerTest {

    private static final String TEST_USER = "user@example.com";

    @Mock
    private JaxRsDpsLog logger;
    @Mock
    private DpsHeaders headers;
    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuditLogger auditLogger;

    private List<String> resources;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(headers.getUserEmail()).thenReturn(TEST_USER);
        when(headers.getUserAuthorizedGroupName()).thenReturn(SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS);
        when(httpRequest.getHeader(Mockito.anyString())).thenReturn("JUnitAgent");
        resources = Collections.singletonList("test-resource");
    }

    @Test
    public void should_writeSchemaRegisteredSuccessEvent() {
        auditLogger.schemaRegisteredSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSchemaRegisteredFailureEvent() {
        auditLogger.schemaRegisteredFailure(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSystemSchemaRegisteredSuccessEvent() {
        auditLogger.systemSchemaRegisteredSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSystemSchemaRegisteredFailureEvent() {
        auditLogger.systemSchemaRegisteredFailure(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSchemaRetrievedSuccessEvent() {
        auditLogger.schemaRetrievedSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSchemaRetrievedFailureEvent() {
        auditLogger.schemaRetrievedFailure(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSearchSchemaSuccessEvent() {
        auditLogger.searchSchemaSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSearchSchemaFailureEvent() {
        auditLogger.searchSchemaFailure(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSchemaUpdatedSuccessEvent() {
        auditLogger.schemaUpdatedSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSchemaUpdatedFailureEvent() {
        auditLogger.schemaUpdatedFailure(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSystemSchemaUpdatedSuccessEvent() {
        auditLogger.systemSchemaUpdatedSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSystemSchemaUpdatedFailureEvent() {
        auditLogger.systemSchemaUpdatedFailure(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSchemaNotificationSuccessEvent() {
        auditLogger.schemaNotificationSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSchemaNotificationFailureEvent() {
        auditLogger.schemaNotificationFailure(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSystemSchemaNotificationSuccessEvent() {
        auditLogger.systemSchemaNotificationSuccess(resources);
        verify(logger, times(1)).audit(any());
    }

    @Test
    public void should_writeSystemSchemaNotificationFailureEvent() {
        auditLogger.systemSchemaNotificationFailure(resources);
        verify(logger, times(1)).audit(any());
    }
}
