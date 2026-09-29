package org.opengroup.osdu.schema.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadRequestException extends RuntimeException {

	private final HttpStatus status;
	private final String errorMsg;
	private static final long serialVersionUID = 5528317674234634627L;

	public BadRequestException(HttpStatus status, String errorMsg, Throwable throwable) {
		super(errorMsg, throwable);
		this.errorMsg = errorMsg;
		this.status = status;
	}

	public BadRequestException(HttpStatus status, String errorMsg) {
		this(status, errorMsg, null);
	}

	public BadRequestException(String errorMsg, Throwable throwable) {
		this(HttpStatus.BAD_REQUEST, errorMsg, throwable);
	}

	public BadRequestException(String errorMsg) {
		this(HttpStatus.BAD_REQUEST, errorMsg, null);
	}

	public BadRequestException(Throwable throwable) {
		this(HttpStatus.BAD_REQUEST.toString(), throwable);
	}

	public BadRequestException() {
		this(HttpStatus.BAD_REQUEST.toString());
	}
}