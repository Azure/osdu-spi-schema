package org.opengroup.osdu.schema.errors.model;

import org.opengroup.osdu.schema.errors.ErrorDetails;

public class MediaTypeError extends ErrorDetails {

    public MediaTypeError(String message) {
        this.setMessage(message);
        this.setDomain("global");
        this.setReason("unsupportedMediaType");
    }
}