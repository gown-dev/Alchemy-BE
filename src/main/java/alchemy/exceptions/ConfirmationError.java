package alchemy.exceptions;

public interface ConfirmationError {
	
	String getCode();
	String getDescription();
	String getMessage();
	String getConfirmationKey();

}
