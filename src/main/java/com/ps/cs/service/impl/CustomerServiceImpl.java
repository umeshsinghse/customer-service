package com.ps.cs.service.impl;

import com.ps.cs.model.Customer;
import com.ps.cs.repository.CustomerRepository;
import com.ps.cs.service.CustomerService;
import com.ps.cs.service.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of CustomerService using repository pattern.
 * Delegates data access operations to CustomerRepository.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final LoggingService loggingService;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, LoggingService loggingService) {
        this.customerRepository = customerRepository;
        this.loggingService = loggingService;
    }

    @Override
    public List<Customer> getAllCustomers() {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Retrieving all customers from repository");
            List<Customer> customers = customerRepository.findAll();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logPerformanceMetric("service.getAllCustomers", executionTime,
                    Map.of("customerCount", customers.size()));

            logger.info("Successfully retrieved {} customers", customers.size());
            return customers;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("service.getAllCustomers", "Failed to retrieve customers", e,
                    Map.of("executionTime", executionTime));
            throw e;
        }
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Retrieving customer by ID: {}", id);
            Optional<Customer> customer = customerRepository.findById(id);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logPerformanceMetric("service.getCustomerById", executionTime,
                    Map.of("customerId", id, "found", customer.isPresent()));

            if (customer.isPresent()) {
                logger.info("Customer found with ID: {}", id);
            } else {
                logger.warn("Customer not found with ID: {}", id);
            }

            return customer;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("service.getCustomerById", "Failed to retrieve customer", e,
                    Map.of("customerId", id, "executionTime", executionTime));
            throw e;
        }
    }

    @Override
    public Customer createCustomer(Customer customer) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Creating new customer: {}", customer.getName());
            Customer savedCustomer = customerRepository.save(customer);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logPerformanceMetric("service.createCustomer", executionTime,
                    Map.of("customerId", savedCustomer.getId(), "customerName", savedCustomer.getName()));

            logger.info("Successfully created customer with ID: {}", savedCustomer.getId());
            return savedCustomer;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("service.createCustomer", "Failed to create customer", e,
                    Map.of("customerName", customer.getName(), "executionTime", executionTime));
            throw e;
        }
    }

    @Override
    public Optional<Customer> updateCustomer(Long id, Customer updatedCustomer) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Updating customer with ID: {}", id);

            if (customerRepository.existsById(id)) {
                updatedCustomer.setId(id);
                Customer savedCustomer = customerRepository.save(updatedCustomer);

                long executionTime = System.currentTimeMillis() - startTime;
                loggingService.logPerformanceMetric("service.updateCustomer", executionTime,
                        Map.of("customerId", id, "updated", true));

                logger.info("Successfully updated customer with ID: {}", id);
                return Optional.of(savedCustomer);
            } else {
                long executionTime = System.currentTimeMillis() - startTime;
                loggingService.logPerformanceMetric("service.updateCustomer", executionTime,
                        Map.of("customerId", id, "updated", false));

                logger.warn("Customer not found for update with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("service.updateCustomer", "Failed to update customer", e,
                    Map.of("customerId", id, "executionTime", executionTime));
            throw e;
        }
    }

    @Override
    public boolean deleteCustomer(Long id) {
        long startTime = System.currentTimeMillis();
        try {
            logger.debug("Deleting customer with ID: {}", id);
            boolean deleted = customerRepository.deleteById(id);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logPerformanceMetric("service.deleteCustomer", executionTime,
                    Map.of("customerId", id, "deleted", deleted));

            if (deleted) {
                logger.info("Successfully deleted customer with ID: {}", id);
            } else {
                logger.warn("Customer not found for deletion with ID: {}", id);
            }

            return deleted;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logError("service.deleteCustomer", "Failed to delete customer", e,
                    Map.of("customerId", id, "executionTime", executionTime));
            throw e;
        }
    }
}