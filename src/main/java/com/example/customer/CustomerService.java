package com.example.customer;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Simple Customer service with hardcoded data.
 * No database, no repositories - just in-memory list.
 */
@Service
public class CustomerService {

    // Hardcoded list of 3 customers
    private final List<Customer> customers = Arrays.asList(
            new Customer(1L, "John Doe", "john.doe@example.com"),
            new Customer(2L, "Jane Smith", "jane.smith@example.com"),
            new Customer(3L, "Bob Johnson", "bob.johnson@example.com")
    );

    /**
     * Get all customers.
     * @return List of all customers
     */
    public List<Customer> getAllCustomers() {
        return customers;
    }

    /**
     * Get customer by ID.
     * @param id Customer ID
     * @return Optional containing customer if found, empty otherwise
     */
    public Optional<Customer> getCustomerById(Long id) {
        return customers.stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst();
    }
}