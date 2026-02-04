package com.ps.cs.service;

import com.ps.cs.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Customer operations.
 * Defines the contract for customer business logic.
 */
public interface CustomerService {
    
    /**
     * Get all customers.
     * @return List of all customers
     */
    List<Customer> getAllCustomers();
    
    /**
     * Get customer by ID.
     * @param id Customer ID
     * @return Optional containing customer if found, empty otherwise
     */
    Optional<Customer> getCustomerById(Long id);
    
    /**
     * Create a new customer.
     * @param customer Customer to create
     * @return Created customer
     */
    Customer createCustomer(Customer customer);
    
    /**
     * Update an existing customer.
     * @param id Customer ID
     * @param customer Updated customer data
     * @return Updated customer if found, empty otherwise
     */
    Optional<Customer> updateCustomer(Long id, Customer customer);
    
    /**
     * Delete a customer by ID.
     * @param id Customer ID
     * @return true if deleted, false if not found
     */
    boolean deleteCustomer(Long id);
}