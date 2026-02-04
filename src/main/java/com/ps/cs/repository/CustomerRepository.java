package com.ps.cs.repository;

import com.ps.cs.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer data access operations.
 * Defines the contract for customer data persistence.
 * This is prepared for future database integration.
 */
public interface CustomerRepository {
    
    /**
     * Find all customers.
     * @return List of all customers
     */
    List<Customer> findAll();
    
    /**
     * Find customer by ID.
     * @param id Customer ID
     * @return Optional containing customer if found
     */
    Optional<Customer> findById(Long id);
    
    /**
     * Find customers by name (partial match).
     * @param name Customer name to search
     * @return List of customers matching the name
     */
    List<Customer> findByNameContaining(String name);
    
    /**
     * Find customer by email.
     * @param email Customer email
     * @return Optional containing customer if found
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Save a customer (create or update).
     * @param customer Customer to save
     * @return Saved customer
     */
    Customer save(Customer customer);
    
    /**
     * Delete customer by ID.
     * @param id Customer ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(Long id);
    
    /**
     * Check if customer exists by ID.
     * @param id Customer ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);
    
    /**
     * Count total number of customers.
     * @return Total count
     */
    long count();
}