package alchemy.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@SuppressWarnings("serial")
@RequiredArgsConstructor
@Getter
@Builder
@ToString
public class AuthException extends RuntimeException {

	private final AuthError error;
	private final HttpStatus httpStatus;
	
}
