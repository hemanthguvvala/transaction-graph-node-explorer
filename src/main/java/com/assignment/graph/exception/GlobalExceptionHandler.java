package com.assignment.graph.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.assignment.graph.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NodeNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NodeNotFoundException ex){
		ErrorResponse error  = new ErrorResponse("NODE_NOT_FOUND", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}
	
	@ExceptionHandler(CycleDetectedException.class)
	public ResponseEntity<ErrorResponse> handleCycle(CycleDetectedException ex){
		ErrorResponse error  = new ErrorResponse("CYCLE_DETECTED", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(InvalidParameterException.class)
	public ResponseEntity<ErrorResponse> handleInvalidParameter(InvalidParameterException ex){
		ErrorResponse error = new ErrorResponse("INVALID_PARAMETER", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex){
		ErrorResponse error = new ErrorResponse("INVALID_PARAMETER",
				"Invalid value for parameter '" + ex.getName() + "'");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}
}
