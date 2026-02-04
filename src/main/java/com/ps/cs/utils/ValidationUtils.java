package com.ps.cs.utils;

import com.ps.cs.model.Customer;

import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 * Contains common validation methods used across the application.
 */
public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Validate if email format is correct.
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate if string is not null or empty.
     * @param value String to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate if ID is valid (positive number).
     * @param id ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }
    
    /**
     * Validate customer object for creation.
     * @param customer Customer to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCustomerForCreation(Customer customer) {
        if (customer == null) {
            return false;
        }
        
        return isNotNullOrEmpty(customer.getName()) &&
               isValidEmail(customer.getEmail());
    }
    
    /**
     * Validate customer object for update.
     * @param customer Customer to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCustomerForUpdate(Customer customer) {
        if (customer == null) {
            return false;
        }
        
        return isNotNullOrEmpty(customer.getName()) &&
               isValidEmail(customer.getEmail());
    }
    
    /**
     * Sanitize string input by trimming whitespace.
     * @param input Input string
     * @return Sanitized string or null if input is null
     */
    public static String sanitizeString(String input) {
        return input != null ? input.trim() : null;
    }
    
    /**
     * Normalize email to lowercase.
     * @param email Email to normalize
     * @return Normalized email or null if input is null
     */
    public static String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }
}