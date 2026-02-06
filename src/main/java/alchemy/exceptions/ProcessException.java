package alchemy.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("serial")
@Getter
@Builder
@ToString
public class ProcessException extends RuntimeException {

	private final ProcessError error;
	private final HttpStatus httpStatus;
	private final String[] parameters;

	public ProcessException(ProcessError error, HttpStatus status) {
		this.error = error;
		this.httpStatus = status;
		this.parameters = new String[0];
	}

	public ProcessException(ProcessError error, HttpStatus status, String... parameters) {
		this.error = error;
		this.httpStatus = status;
		this.parameters = parameters;
	}

}
