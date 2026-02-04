package com.ps.cs.controller;

import com.ps.cs.model.Customer;
import com.ps.cs.service.CustomerService;
import com.ps.cs.service.LoggingService;
import com.ps.cs.health.CloudWatchHealthIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * REST controller for Customer API endpoints.
 * Handles HTTP requests and delegates business logic to CustomerService.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final LoggingService loggingService;
    private final CloudWatchHealthIndicator cloudWatchHealthIndicator;

    @Autowired
    public CustomerController(CustomerService customerService, LoggingService loggingService,
                              CloudWatchHealthIndicator cloudWatchHealthIndicator) {
        this.customerService = customerService;
        this.loggingService = loggingService;
        this.cloudWatchHealthIndicator = cloudWatchHealthIndicator;
    }

    /**
     * GET /api/customers - Return list of all customers
     */
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        long startTime = System.currentTimeMillis();
        try {
            logger.info("Fetching all customers");
            List<Customer> customers = customerService.getAllCustomers();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessEvent("GET_ALL", "Customer", null,
                    "Retrieved " + customers.size() + " customers", true);
            loggingService.logPerformanceMetric("getAllCustomers", executionTime,
                    Map.of("customerCount", customers.size()));

            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("getAllCustomers", "Failed to fetch customers", e,
                    Map.of("executionTime", executionTime));
            throw e;
        }
    }

    /**
     * GET /api/customers/{id} - Return single customer by ID
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        try {
            logger.info("Fetching customer with ID: {}", id);
            Optional<Customer> customer = customerService.getCustomerById(id);

            long executionTime = System.currentTimeMillis() - startTime;
            boolean found = customer.isPresent();

            loggingService.logBusinessEvent("GET_BY_ID", "Customer", id,
                    found ? "Customer found" : "Customer not found", found);
            loggingService.logPerformanceMetric("getCustomerById", executionTime,
                    Map.of("customerId", id, "found", found));

            return customer.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("getCustomerById", "Failed to fetch customer by ID", e,
                    Map.of("customerId", id, "executionTime", executionTime));
            throw e;
        }
    }

    /**
     * POST /api/customers - Create a new customer
     */
    @PostMapping("/customers")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        long startTime = System.currentTimeMillis();
        try {
            logger.info("Creating new customer: {}", customer.getName());
            Customer createdCustomer = customerService.createCustomer(customer);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessEvent("CREATE", "Customer", createdCustomer.getId(),
                    "Customer created successfully: " + createdCustomer.getName(), true);
            loggingService.logPerformanceMetric("createCustomer", executionTime,
                    Map.of("customerId", createdCustomer.getId(), "customerName", createdCustomer.getName()));

            return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("createCustomer", "Failed to create customer", e,
                    Map.of("customerName", customer.getName(), "executionTime", executionTime));
            loggingService.logBusinessEvent("CREATE", "Customer", null,
                    "Failed to create customer: " + e.getMessage(), false);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/customers/{id} - Update an existing customer
     */
    @PutMapping("/customers/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        long startTime = System.currentTimeMillis();
        try {
            logger.info("Updating customer with ID: {}", id);
            Optional<Customer> updatedCustomer = customerService.updateCustomer(id, customer);

            long executionTime = System.currentTimeMillis() - startTime;
            boolean updated = updatedCustomer.isPresent();

            loggingService.logBusinessEvent("UPDATE", "Customer", id,
                    updated ? "Customer updated successfully" : "Customer not found for update", updated);
            loggingService.logPerformanceMetric("updateCustomer", executionTime,
                    Map.of("customerId", id, "updated", updated));

            return updatedCustomer.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("updateCustomer", "Failed to update customer", e,
                    Map.of("customerId", id, "executionTime", executionTime));
            throw e;
        }
    }

    /**
     * DELETE /api/customers/{id} - Delete a customer by ID
     */
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        try {
            logger.info("Deleting customer with ID: {}", id);
            boolean deleted = customerService.deleteCustomer(id);

            long executionTime = System.currentTimeMillis() - startTime;

            loggingService.logBusinessEvent("DELETE", "Customer", id,
                    deleted ? "Customer deleted successfully" : "Customer not found for deletion", deleted);
            loggingService.logPerformanceMetric("deleteCustomer", executionTime,
                    Map.of("customerId", id, "deleted", deleted));

            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("deleteCustomer", "Failed to delete customer", e,
                    Map.of("customerId", id, "executionTime", executionTime));
            throw e;
        }
    }

    /**
     * GET /health - Simple health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            // Perform basic health checks
            boolean serviceHealthy = customerService.getAllCustomers() != null;

            // Check CloudWatch connectivity
            Map<String, Object> cloudWatchHealth = cloudWatchHealthIndicator.checkCloudWatchHealth();
            boolean cloudWatchHealthy = "UP".equals(cloudWatchHealth.get("status"));

            Map<String, Object> healthStatus = new HashMap<>();
            healthStatus.put("status", (serviceHealthy && cloudWatchHealthy) ? "UP" : "DOWN");
            healthStatus.put("service", "customer-service");
            healthStatus.put("timestamp", String.valueOf(System.currentTimeMillis()));
            healthStatus.put("version", "1.0.0");
            healthStatus.put("components", Map.of(
                    "customerService", serviceHealthy ? "UP" : "DOWN",
                    "cloudWatch", cloudWatchHealth
            ));

            loggingService.logHealthCheck("customer-service", serviceHealthy && cloudWatchHealthy,
                    "Health check completed - Service: " + (serviceHealthy ? "UP" : "DOWN") +
                            ", CloudWatch: " + (cloudWatchHealthy ? "UP" : "DOWN"));

            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            loggingService.logError("healthCheck", "Health check failed", e,
                    Map.of("component", "customer-service"));

            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("status", "DOWN");
            errorStatus.put("service", "customer-service");
            errorStatus.put("timestamp", String.valueOf(System.currentTimeMillis()));
            errorStatus.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorStatus);
        }
    }
}