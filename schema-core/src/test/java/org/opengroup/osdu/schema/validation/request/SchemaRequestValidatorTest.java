package org.opengroup.osdu.schema.validation.request;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.schema.validation.request.SchemaRequestValidator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaRequestValidatorTest {

    @InjectMocks
    SchemaRequestValidator schemaValidator;

    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    private Object schemaInputObject;

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test_JsonArrayAsInput_Schema() throws JsonMappingException, JsonProcessingException {
        schemaInputObject = mapper.readValue("[{}]", Object.class);
        assertEquals(false, schemaValidator.isValid(schemaInputObject, constraintValidatorContext));
    }

    @Test
    public void test_JsonObjectAsInput_Schema() throws JsonMappingException, JsonProcessingException {
        schemaInputObject = mapper.readValue("{}", Object.class);
        assertEquals(true, schemaValidator.isValid(schemaInputObject, constraintValidatorContext));
    }

}