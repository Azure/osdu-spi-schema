package org.opengroup.osdu.schema.errors.model;

import org.opengroup.osdu.schema.errors.ErrorDetails;

public class AuthorizationError extends ErrorDetails {

    public AuthorizationError(String message) {
        this.setMessage(message);
        this.setDomain("global");
        this.setReason("forbidden");
    }
}