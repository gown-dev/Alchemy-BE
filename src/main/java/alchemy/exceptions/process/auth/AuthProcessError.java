package alchemy.exceptions.process.auth;

import alchemy.exceptions.ProcessError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum AuthProcessError implements ProcessError {

	UNAUTHENTICATED("ERR_AUTH-F001", "Unauthenticated", "Unable to read the caller account object."),
	MALFORMED_AUTH("ERR_AUTH-F002", "Malformed authentication", "The authentication object was malformed, and the application was unable to read it."),
	
	USERNAME_UNSUITABLE("ERR_AUTH-T003", "Unsuitable username", "Username did not comply with the restrictions for registration."),
	PASSWORD_UNSUITABLE("ERR_AUTH-F002", "Unsuitable password", "Password did not comply with the restrictions for registration."),
	USERNAME_TAKEN("ERR_AUTH-F003", "Username already in use", "Username was already used for another Account."),
	USERNAME_AND_TAG_TAKEN("ERR_AUTH-F004", "Username already in use", "Username was already used for another Account."),
	MISSING_OR_INVALID_REFRESH_TOKEN("ERR_AUTH-F005", "Missing or invalid refresh token", "Refresh token was found to be null or expired."),
	MALFORMED_REFRESH_TOKEN("ERR_AUTH-F006", "Malformed refresh token", "Refresh token was not coherent with the expected format."),
	INVALID_CREDENTIALS("ERR_AUTH-F007", "Invalid credentials", "Unable to authenticate the user with the provided credentials."),
	MISSING_USERNAME("ERR_AUTH-F008", "Missing username", "The request is missing a username input."),
	MISSING_TAG("ERR_AUTH-F009", "Missing tag", "The request is missing a tag input."),
	MISSING_PASSWORD("ERR_AUTH-F010", "Missing password", "The request is missing a password input.");

	public String code;
	public String description;
	public String message;
	
}
