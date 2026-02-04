package com.ps.cs.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating standardized HTTP responses.
 * Provides methods to create consistent API responses.
 */
public class ResponseUtils {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private ResponseUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Create a success response with data.
     * @param data Response data
     * @param <T> Type of data
     * @return ResponseEntity with success status
     */
    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }
    
    /**
     * Create a created response with data.
     * @param data Response data
     * @param <T> Type of data
     * @return ResponseEntity with created status
     */
    public static <T> ResponseEntity<T> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }
    
    /**
     * Create a not found response.
     * @param <T> Type of data
     * @return ResponseEntity with not found status
     */
    public static <T> ResponseEntity<T> notFound() {
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Create a bad request response.
     * @param <T> Type of data
     * @return ResponseEntity with bad request status
     */
    public static <T> ResponseEntity<T> badRequest() {
        return ResponseEntity.badRequest().build();
    }
    
    /**
     * Create a no content response.
     * @return ResponseEntity with no content status
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Create an error response with message.
     * @param status HTTP status
     * @param message Error message
     * @return ResponseEntity with error details
     */
    public static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("status", status.value());
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Create a validation error response.
     * @param message Validation error message
     * @return ResponseEntity with validation error details
     */
    public static ResponseEntity<Map<String, Object>> validationError(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }
    
    /**
     * Create an internal server error response.
     * @param message Error message
     * @return ResponseEntity with internal server error details
     */
    public static ResponseEntity<Map<String, Object>> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
    
    /**
     * Create a success response with message.
     * @param message Success message
     * @return ResponseEntity with success details
     */
    public static ResponseEntity<Map<String, Object>> successMessage(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        
        return ResponseEntity.ok(response);
    }
}