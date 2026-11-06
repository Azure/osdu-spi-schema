package org.opengroup.osdu.schema.errors;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
@JsonPropertyOrder({"domain", "reason", "message"})
public abstract class ErrorDetails {

    private String domain;
    private String reason;
    private String message;
}
