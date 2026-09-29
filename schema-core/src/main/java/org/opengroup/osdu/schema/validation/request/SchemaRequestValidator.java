package org.opengroup.osdu.schema.validation.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Method to validate if field with SchemaConstraint annotation is in form of
 * Json Object
 */
public class SchemaRequestValidator implements ConstraintValidator<SchemaRequestConstraint, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String schemaString = mapper.writeValueAsString(value);
            new JSONObject(schemaString);
        } catch (JsonProcessingException e) {
            return false;
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

}
