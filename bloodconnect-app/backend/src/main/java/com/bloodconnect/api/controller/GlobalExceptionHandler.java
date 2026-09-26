package com.bloodconnect.api.controller;

import com.bloodconnect.api.dto.ErrorResponse;
import com.bloodconnect.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
        ResourceNotFoundException ex,
        WebRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .errorCode("RESOURCE_NOT_FOUND")
            .timestamp(System.currentTimeMillis())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
        BadRequestException ex,
        WebRequest request) {
        
        log.error("Bad request: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .errorCode("BAD_REQUEST")
            .timestamp(System.currentTimeMillis())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
        UnauthorizedException ex,
        WebRequest request) {
        
        log.error("Unauthorized: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .errorCode("UNAUTHORIZED")
            .timestamp(System.currentTimeMillis())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
        ForbiddenException ex,
        WebRequest request) {
        
        log.error("Forbidden: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .errorCode("FORBIDDEN")
            .timestamp(System.currentTimeMillis())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
        MethodArgumentNotValidException ex,
        WebRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation failed");
        response.put("errorCode", "VALIDATION_ERROR");
        response.put("timestamp", System.currentTimeMillis());
        response.put("errors", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
        AuthenticationException ex,
        WebRequest request) {
        
        log.error("Authentication error: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .message("Authentication failed")
            .errorCode("AUTHENTICATION_ERROR")
            .timestamp(System.currentTimeMillis())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
        Exception ex,
        WebRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .message("An unexpected error occurred")
            .errorCode("INTERNAL_SERVER_ERROR")
            .timestamp(System.currentTimeMillis())
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
