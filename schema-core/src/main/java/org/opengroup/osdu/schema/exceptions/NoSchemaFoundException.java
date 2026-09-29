package org.opengroup.osdu.schema.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoSchemaFoundException extends BadRequestException {
	


	private final HttpStatus status;
	private final String errorMsg;
	private static final long serialVersionUID = 5528317674234634627L;

	public NoSchemaFoundException(HttpStatus status, String errorMsg, Throwable throwable) {
		super(errorMsg, throwable);
		this.errorMsg = errorMsg;
		this.status = status;
	}

	public NoSchemaFoundException(HttpStatus status, String errorMsg) {
		this(status, errorMsg, null);
	}

	public NoSchemaFoundException(String errorMsg, Throwable throwable) {
		this(HttpStatus.BAD_REQUEST, errorMsg, throwable);
	}

	public NoSchemaFoundException(String errorMsg) {
		this(HttpStatus.BAD_REQUEST, errorMsg, null);
	}

	public NoSchemaFoundException(Throwable throwable) {
		this(HttpStatus.BAD_REQUEST.toString(), throwable);
	}

	public NoSchemaFoundException() {
		this(HttpStatus.BAD_REQUEST.toString());
	}


}
