package org.opengroup.osdu.schema.errors.model;

import org.opengroup.osdu.schema.errors.ErrorDetails;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BadRequestError extends ErrorDetails {

    public BadRequestError(String message) {
        this.setMessage(message);
        this.setDomain("global");
        this.setReason("badRequest");
    }
}
