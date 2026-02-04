package com.ps.cs.interceptor;

import com.ps.cs.service.LoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to automatically log all API requests and responses.
 * Integrates with CloudWatch logging for comprehensive API monitoring.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private final LoggingService loggingService;
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Autowired
    public LoggingInterceptor(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Record start time for response time calculation
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        // Calculate response time
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long responseTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

        // Extract request information
        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        int statusCode = response.getStatus();
        
        // Extract customer ID from path if present
        Long customerId = extractCustomerIdFromPath(endpoint);

        // Log the API request
        loggingService.logApiRequest(method, endpoint, clientIp, customerId, responseTime, statusCode);

        // Log errors if any
        if (ex != null) {
            loggingService.logError("API_REQUEST", "Request processing failed", ex, 
                java.util.Map.of(
                    "method", method,
                    "endpoint", endpoint,
                    "statusCode", statusCode,
                    "responseTime", responseTime
                ));
        }
    }

    /**
     * Extract client IP address from request headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Extract customer ID from URL path if present
     */
    private Long extractCustomerIdFromPath(String path) {
        try {
            // Pattern: /api/customers/{id}
            if (path.matches(".*/customers/\\d+.*")) {
                String[] pathParts = path.split("/");
                for (int i = 0; i < pathParts.length - 1; i++) {
                    if ("customers".equals(pathParts[i]) && i + 1 < pathParts.length) {
                        String idPart = pathParts[i + 1];
                        // Remove any query parameters
                        if (idPart.contains("?")) {
                            idPart = idPart.substring(0, idPart.indexOf("?"));
                        }
                        return Long.parseLong(idPart);
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Ignore parsing errors
        }
        return null;
    }
}