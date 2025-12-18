package alchemy.exceptions;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import alchemy.model.ConfirmationResponseDTO;
import alchemy.model.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class AlchemyExceptionHandler {
	
	@ExceptionHandler(ConfirmationException.class)
	public ResponseEntity<Object> handleConfirmationResponseStatus(ConfirmationException ex, HttpServletRequest request, HttpServletResponse response) {
		ConfirmationResponseDTO error = new ConfirmationResponseDTO(
				ex.getError().getCode(),
				ex.getError().getDescription(),
        		MessageFormatter.arrayFormat(ex.getError().getMessage(), ex.getParameters()).getMessage(),
				ex.getError().getConfirmationKey());
		return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(error);
	}
	
	@ExceptionHandler(ProcessException.class)
    public ResponseEntity<Object> handleResponseStatus(ProcessException ex, HttpServletRequest request, HttpServletResponse response) {
        ErrorResponseDTO error = new ErrorResponseDTO(
        		ex.getError().getCode(), 
        		ex.getError().getDescription(), 
        		MessageFormatter.arrayFormat(ex.getError().getMessage(), ex.getParameters()).getMessage());
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