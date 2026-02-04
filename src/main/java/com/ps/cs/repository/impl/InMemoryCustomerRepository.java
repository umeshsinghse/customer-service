package com.ps.cs.repository.impl;

import com.ps.cs.model.Customer;
import com.ps.cs.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of CustomerRepository.
 * This is a simple implementation for demonstration purposes.
 * In a real application, this would be replaced with JPA/database implementation.
 */
@Repository
public class InMemoryCustomerRepository implements CustomerRepository {
    
    private final List<Customer> customers = new ArrayList<>(Arrays.asList(
            new Customer(1L, "John Doe", "john.doe@example.com"),
            new Customer(2L, "Jane Smith", "jane.smith@example.com"),
            new Customer(3L, "Bob Johnson", "bob.johnson@example.com")
    ));
    
    private final AtomicLong idGenerator = new AtomicLong(4L);
    
    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(customers);
    }
    
    @Override
    public Optional<Customer> findById(Long id) {
        return customers.stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst();
    }
    
    @Override
    public List<Customer> findByNameContaining(String name) {
        return customers.stream()
                .filter(customer -> customer.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Customer> findByEmail(String email) {
        return customers.stream()
                .filter(customer -> customer.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
    
    @Override
    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            // Create new customer
            customer.setId(idGenerator.getAndIncrement());
            customers.add(customer);
        } else {
            // Update existing customer
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getId().equals(customer.getId())) {
                    customers.set(i, customer);
                    break;
                }
            }
        }
        return customer;
    }
    
    @Override
    public boolean deleteById(Long id) {
        return customers.removeIf(customer -> customer.getId().equals(id));
    }
    
    @Override
    public boolean existsById(Long id) {
        return customers.stream()
                .anyMatch(customer -> customer.getId().equals(id));
    }
    
    @Override
    public long count() {
        return customers.size();
    }
}