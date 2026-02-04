package com.ps.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Customer Service.
 * Entry point for the customer service microservice.
 * 
 * @author Publicis Sapient
 * @version 1.0.0
 */
@SpringBootApplication
public class CustomerApplication {
    
    /**
     * Main method to start the Spring Boot application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}