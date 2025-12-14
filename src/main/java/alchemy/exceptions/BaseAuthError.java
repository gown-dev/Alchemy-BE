package alchemy.exceptions;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum BaseAuthError implements AuthError {

	UNAUTHENTICATED("ERR_T001", "Unauthenticated", "Unable to read the caller account object."),
	MALFORMED_AUTH("ERR_T002", "Malformed authentication", "The authentication object was malformed, and the application was unable to read it."),
	
	USERNAME_UNSUITABLE("ERR_F001", "Unsuitable username", "Username did not comply with the restrictions for registration."),
	PASSWORD_UNSUITABLE("ERR_F002", "Unsuitable password", "Password did not comply with the restrictions for registration."),
	USERNAME_TAKEN("ERR_F003", "Username already in use", "Username was already used for another Account."),
	USERNAME_AND_TAG_TAKEN("ERR_F004", "Username already in use", "Username was already used for another Account."),
	MISSING_OR_INVALID_REFRESH_TOKEN("ERR_F005", "Missing or invalid refresh token", "Refresh token was found to be null or expired."),
	MALFORMED_REFRESH_TOKEN("ERR_F006", "Malformed refresh token", "Refresh token was not coherent with the expected format."),
	INVALID_CREDENTIALS("ERR_F007", "Invalid credentials", "Unable to authenticate the user with the provided credentials."),
	MISSING_USERNAME("ERR_F008", "Missing username", "The request is missing a username input."),
	MISSING_TAG("ERR_F009", "Missing tag", "The request is missing a tag input."),
	MISSING_PASSWORD("ERR_F010", "Missing password", "The request is missing a password input.");

	public String code;
	public String description;
	public String message;
	
	BaseAuthError(String code, String description, String message) {
		this.code = code;
		this.description = description;
		this.message = message;
	}
	
}
