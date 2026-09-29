package org.opengroup.osdu.schema.exceptions;

import org.springframework.http.HttpStatus;

public class SchemaVersionException extends BadRequestException{

	private final HttpStatus status;
	private final String errorMsg;
	private static final long serialVersionUID = 5234324334634627L;

	public SchemaVersionException(HttpStatus status, String errorMsg, Throwable throwable) {
		super(errorMsg, throwable);
		this.errorMsg = errorMsg;
		this.status = status;
	}

	public SchemaVersionException(HttpStatus status, String errorMsg) {
		this(status, errorMsg, null);
	}

	public SchemaVersionException(String errorMsg, Throwable throwable) {
		this(HttpStatus.BAD_REQUEST, errorMsg, throwable);
	}

	public SchemaVersionException(String errorMsg) {
		this(HttpStatus.BAD_REQUEST, errorMsg, null);
	}

	public SchemaVersionException(Throwable throwable) {
		this(HttpStatus.BAD_REQUEST.toString(), throwable);
	}

	public SchemaVersionException() {
		this(HttpStatus.BAD_REQUEST.toString());
	}
}
