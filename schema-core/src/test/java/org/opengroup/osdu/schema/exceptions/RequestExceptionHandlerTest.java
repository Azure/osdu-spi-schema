package org.opengroup.osdu.schema.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.schema.errors.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestExceptionHandlerTest {

    @InjectMocks
    private RequestExceptionHandler handler;

    @Mock
    private WebRequest mockRequest;

    @Mock
    private JaxRsDpsLog log;

    @BeforeEach
    public void setup() {
        mockRequest = Mockito.mock(WebRequest.class);
        Mockito.when(mockRequest.getHeader("correlation-id")).thenReturn("sample-id");
    }

    @Test
    public void testNotFoundExHasCorrectMsg() {
        String errMsg = "Resource Not Found";
        NotFoundException ex = new NotFoundException(errMsg);
        ResponseEntity<Object> response = handler.handleNotFoundException(ex, mockRequest);
        Assertions.assertNotNull(response);
        Error error = (Error) response.getBody();
        Assertions.assertNotNull(error.getMessage());
        Assertions.assertTrue(error.getMessage().equals(errMsg));
    }

    @Test
    public void testForbiddenOnAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("access denied");
        ResponseEntity<Object> response = handler.handleAccessDeniedException(ex, mockRequest);
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testNotFoundExHasCorrectStatus() {
        NotFoundException ex = new NotFoundException();
        ResponseEntity<Object> response = handler.handleNotFoundException(ex, mockRequest);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testCorrectCrlIdWithWebRequest() {
        Assertions.assertEquals("sample-id : ", handler.getCorrelationId(mockRequest));
    }

    @Test
    public void testEmptyCrlIdWhenWebRequestIsNull() {
        WebRequest request = null;
        Assertions.assertEquals("", handler.getCorrelationId(request));
    }

    @Test
    public void testCorrectHeaderIsExtracted() {
        String headerName = handler.extractMissingHeaderName("The requred header 'Authorization' is missing");
        Assertions.assertEquals("Authorization", headerName);
    }

    @Test
    public void testFirstCorrectHeaderIsExtracted() {
        String headerName = handler
                .extractMissingHeaderName("The requred header 'Authorization', 'account' is missing");
        Assertions.assertEquals("Authorization", headerName);
    }

    @Test
    public void testEmptyWhenNoMsg() {
        String headerName = handler.extractMissingHeaderName(null);
        Assertions.assertEquals("", headerName);

        String headerName2 = handler.extractMissingHeaderName("");
        Assertions.assertEquals("", headerName2);

        String headerName3 = handler.extractMissingHeaderName("Requred header is missing");
        Assertions.assertEquals("", headerName3);
    }
}
