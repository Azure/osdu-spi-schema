package org.opengroup.osdu.schema.validation.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaInfoRequestValidatorTest {

    @InjectMocks
    SchemaInfoRequestValidator schemaInfoRequestValidator;

    @Test
    public void validateRequestWithValidParametersShouldNotThrowException() {
        Set<String> validParams = new HashSet<>(Arrays.asList("authority", "source", "entityType", "schemaVersionMajor", "schemaVersionMinor", "schemaVersionPatch", "status", "scope", "latestVersion", "limit", "offset"));

        assertDoesNotThrow(() -> {
            schemaInfoRequestValidator.validateRequest(validParams);
        });	}

    @Test
    public void validateRequestWithInvalidParametersShouldThrowBadRequestException() {
        Set<String> invalidParams = new HashSet<>(Arrays.asList("invalidParam1", "invalidParam2"));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            schemaInfoRequestValidator.validateRequest(invalidParams);
        });

        String expectedErrorForInvalidParam = "invalidParam1 is not a valid input param!";
        assertEquals(expectedErrorForInvalidParam,exception.getMessage());

        Set<String> typoInRequestParam = new HashSet<>(Arrays.asList("limi"));

        exception = assertThrows(BadRequestException.class, () -> {
            schemaInfoRequestValidator.validateRequest(typoInRequestParam);
        });

        String expectedErrorForTypo = "limi is not a valid input param!";
        assertEquals(expectedErrorForTypo, exception.getMessage());
    }
    @Test
    public void extractQueryParamsFromRequest() {
        // Create a mock HttpServletRequest
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addParameter("param1", "value1");
        mockRequest.addParameter("param2", "value2");

        // Set the mock request attributes
        RequestAttributes requestAttributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        // Act
        Set<String> queryParams = SchemaInfoRequestValidator.extractQueryParamsFromRequest();

        // Assert
        assertEquals(2, queryParams.size());
        assertEquals(Set.of("param1", "param2"), queryParams);
    }
}
