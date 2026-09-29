package org.opengroup.osdu.schema.errors;

import java.util.ArrayList;
import java.util.List;

import org.opengroup.osdu.schema.errors.model.BadRequestError;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME, visible = true)
@JsonTypeName("error")
public class Error {

    @JsonIgnore
    private HttpStatus status;

    private int code;
    private String message;
    private List<ErrorDetails> errors;

    public Error(HttpStatus status) {
        this.status = status;
    }

    public void addErrors(ErrorDetails error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }

    public void addError(String message) {
        addErrors(new BadRequestError(message));
    }

    private void addValidationError(FieldError fieldError) {
        this.addError(fieldError.getDefaultMessage());
    }

    public void addValidationErrors(List<FieldError> fieldErrors) {
        fieldErrors.forEach(this::addValidationError);
    }
}
