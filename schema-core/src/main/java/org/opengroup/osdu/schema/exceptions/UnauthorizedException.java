package org.opengroup.osdu.schema.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = -4700592093175762085L;
    private final HttpStatus status;
    private final String errorMsg;

    public UnauthorizedException(HttpStatus status, String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
        this.errorMsg = errorMsg;
        this.status = status;
    }

    public UnauthorizedException(HttpStatus status, String errorMsg) {
        this(status, errorMsg, null);
    }

    public UnauthorizedException(String errorMsg, Throwable throwable) {
        this(HttpStatus.UNAUTHORIZED, errorMsg, throwable);
    }

    public UnauthorizedException(String errorMsg) {
        this(HttpStatus.UNAUTHORIZED, errorMsg, null);
    }

    public UnauthorizedException(Throwable throwable) {
        this(HttpStatus.UNAUTHORIZED.toString(), throwable);
    }

    public UnauthorizedException() {
        this(HttpStatus.UNAUTHORIZED.toString());
    }
}
