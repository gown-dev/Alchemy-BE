package alchemy.exceptions;

import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("serial")
@Getter
@ToString
public class ConfirmationException extends RuntimeException {

	private final ConfirmationError error;
	private final String[] parameters;

	public ConfirmationException(ConfirmationError error) {
		this.error = error;
		this.parameters = new String[0];
	}

	public ConfirmationException(ConfirmationError error, String... parameters) {
		this.error = error;
		this.parameters = parameters;
	}

}
