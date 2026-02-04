package com.ps.cs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Service for structured logging with CloudWatch integration.
 * Provides methods for logging API requests, business events, and errors.
 */
@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final Logger apiLogger = LoggerFactory.getLogger("API_ACCESS");
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_EVENT");
    private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_EVENT");

    /**
     * Log API request with structured data
     */
    public void logApiRequest(String method, String endpoint, String clientIp, 
                             Long customerId, long responseTime, int statusCode) {
        String requestId = generateRequestId();
        
        try {
            MDC.put("requestId", requestId);
            MDC.put("method", method);
            MDC.put("endpoint", endpoint);
            MDC.put("clientIp", clientIp);
            MDC.put("responseTime", String.valueOf(responseTime));
            MDC.put("statusCode", String.valueOf(statusCode));
            
            if (customerId != null) {
                MDC.put("customerId", String.valueOf(customerId));
            }
            
            apiLogger.info("API Request: {} {} - Status: {} - Response Time: {}ms", 
                         method, endpoint, statusCode, responseTime);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log business events (CRUD operations)
     */
    public void logBusinessEvent(String operation, String entityType, Long entityId, 
                               String details, boolean success) {
        String eventId = generateEventId();
        
        try {
            MDC.put("eventId", eventId);
            MDC.put("operation", operation);
            MDC.put("entityType", entityType);
            MDC.put("success", String.valueOf(success));
            
            if (entityId != null) {
                MDC.put("entityId", String.valueOf(entityId));
            }
            
            if (success) {
                businessLogger.info("Business Event: {} {} {} - {}", 
                                  operation, entityType, entityId != null ? entityId : "N/A", details);
            } else {
                businessLogger.warn("Business Event Failed: {} {} {} - {}", 
                                   operation, entityType, entityId != null ? entityId : "N/A", details);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log application errors with context
     */
    public void logError(String operation, String errorMessage, Exception exception, 
                        Map<String, Object> context) {
        String errorId = generateErrorId();
        
        try {
            MDC.put("errorId", errorId);
            MDC.put("operation", operation);
            MDC.put("errorType", exception != null ? exception.getClass().getSimpleName() : "Unknown");
            
            // Add context information
            if (context != null) {
                context.forEach((key, value) -> {
                    if (value != null) {
                        MDC.put(key, value.toString());
                    }
                });
            }
            
            if (exception != null) {
                errorLogger.error("Application Error in {}: {} - {}", 
                                operation, errorMessage, exception.getMessage(), exception);
            } else {
                errorLogger.error("Application Error in {}: {}", operation, errorMessage);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log performance metrics
     */
    public void logPerformanceMetric(String operation, long executionTime, 
                                   Map<String, Object> metrics) {
        try {
            MDC.put("operation", operation);
            MDC.put("executionTime", String.valueOf(executionTime));
            
            if (metrics != null) {
                metrics.forEach((key, value) -> {
                    if (value != null) {
                        MDC.put(key, value.toString());
                    }
                });
            }
            
            logger.info("Performance Metric: {} completed in {}ms", operation, executionTime);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log health check events
     */
    public void logHealthCheck(String component, boolean healthy, String details) {
        try {
            MDC.put("component", component);
            MDC.put("healthy", String.valueOf(healthy));
            
            if (healthy) {
                logger.info("Health Check: {} is healthy - {}", component, details);
            } else {
                logger.warn("Health Check: {} is unhealthy - {}", component, details);
            }
        } finally {
            MDC.clear();
        }
    }

    private String generateRequestId() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateEventId() {
        return "EVT-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateErrorId() {
        return "ERR-" + UUID.randomUUID().toString().substring(0, 8);
    }
}