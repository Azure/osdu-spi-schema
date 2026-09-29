package org.opengroup.osdu.schema.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationException extends Exception {

    private final HttpStatus status;
    private final String errorMsg;
    private static final long serialVersionUID = 5528317674234634627L;

    public ApplicationException(HttpStatus status, String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
        this.errorMsg = errorMsg;
        this.status = status;
    }

    public ApplicationException(HttpStatus status, String errorMsg) {
        this(status, errorMsg, null);
    }

    public ApplicationException(String errorMsg, Throwable throwable) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg, throwable);
    }

    public ApplicationException(String errorMsg) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg, null);
    }

    public ApplicationException(Throwable throwable) {
        this(HttpStatus.INTERNAL_SERVER_ERROR.toString(), throwable);
    }

    public ApplicationException() {
        this(HttpStatus.INTERNAL_SERVER_ERROR.toString());
    }
}