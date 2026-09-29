package org.opengroup.osdu.schema.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends Exception {

	private static final long serialVersionUID = 3415394375069766727L;
	private final HttpStatus status;
	private final String errorMsg;
	

	public NotFoundException(HttpStatus status, String errorMsg, Throwable throwable) {
		super(errorMsg, throwable);
		this.errorMsg = errorMsg;
		this.status = status;
	}

	public NotFoundException(HttpStatus status, String errorMsg) {
		this(status, errorMsg, null);
	}

	public NotFoundException(String errorMsg, Throwable throwable) {
		this(HttpStatus.NOT_FOUND, errorMsg, throwable);
	}

	public NotFoundException(String errorMsg) {
		this(HttpStatus.NOT_FOUND, errorMsg, null);
	}

	public NotFoundException(Throwable throwable) {
		this(HttpStatus.NOT_FOUND.toString(), throwable);
	}

	public NotFoundException() {
		this(HttpStatus.NOT_FOUND.toString());
	}
}