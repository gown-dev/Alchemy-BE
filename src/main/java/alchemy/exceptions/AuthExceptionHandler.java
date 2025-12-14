package alchemy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import alchemy.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class AuthExceptionHandler {
	
	@ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleResponseStatus(AuthException ex, HttpServletRequest request, HttpServletResponse response) {
        ErrorResponse error = new ErrorResponse(ex.getError().getCode(), ex.getError().getDescription(), ex.getError().getMessage());
		return ResponseEntity.status(ex.getHttpStatus()).contentType(MediaType.APPLICATION_JSON).body(error);
    }
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestNotSupportedException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
    	log.info("[HttpRequestMethodNotSupportedException] : " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, HttpServletRequest request, HttpServletResponse response) {
    	ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
	
}