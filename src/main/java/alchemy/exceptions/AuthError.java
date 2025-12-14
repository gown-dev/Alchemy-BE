package alchemy.exceptions;

public interface AuthError {
	
	String getCode();
	String getDescription();
	String getMessage();

}
