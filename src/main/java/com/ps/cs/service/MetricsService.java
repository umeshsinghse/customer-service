package com.ps.cs.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for publishing custom metrics to CloudWatch.
 * Provides methods for tracking API calls, business operations, and performance metrics.
 */
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    
    // Counters for API operations
    private final Counter customerGetAllCounter;
    private final Counter customerGetByIdCounter;
    private final Counter customerCreateCounter;
    private final Counter customerUpdateCounter;
    private final Counter customerDeleteCounter;
    private final Counter apiErrorCounter;
    
    // Timers for performance tracking
    private final Timer customerOperationTimer;
    private final Timer apiResponseTimer;

    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.customerGetAllCounter = Counter.builder("customer.operations")
                .description("Number of get all customers operations")
                .tag("operation", "get_all")
                .register(meterRegistry);
                
        this.customerGetByIdCounter = Counter.builder("customer.operations")
                .description("Number of get customer by ID operations")
                .tag("operation", "get_by_id")
                .register(meterRegistry);
                
        this.customerCreateCounter = Counter.builder("customer.operations")
                .description("Number of create customer operations")
                .tag("operation", "create")
                .register(meterRegistry);
                
        this.customerUpdateCounter = Counter.builder("customer.operations")
                .description("Number of update customer operations")
                .tag("operation", "update")
                .register(meterRegistry);
                
        this.customerDeleteCounter = Counter.builder("customer.operations")
                .description("Number of delete customer operations")
                .tag("operation", "delete")
                .register(meterRegistry);
                
        this.apiErrorCounter = Counter.builder("api.errors")
                .description("Number of API errors")
                .register(meterRegistry);
        
        // Initialize timers
        this.customerOperationTimer = Timer.builder("customer.operation.duration")
                .description("Duration of customer operations")
                .register(meterRegistry);
                
        this.apiResponseTimer = Timer.builder("api.response.duration")
                .description("API response time")
                .register(meterRegistry);
    }

    /**
     * Increment counter for customer operations
     */
    public void incrementCustomerOperation(String operation) {
        switch (operation.toLowerCase()) {
            case "get_all":
                customerGetAllCounter.increment();
                break;
            case "get_by_id":
                customerGetByIdCounter.increment();
                break;
            case "create":
                customerCreateCounter.increment();
                break;
            case "update":
                customerUpdateCounter.increment();
                break;
            case "delete":
                customerDeleteCounter.increment();
                break;
        }
    }

    /**
     * Record API response time
     */
    public void recordApiResponseTime(long responseTimeMs) {
        apiResponseTimer.record(responseTimeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Record customer operation duration
     */
    public void recordCustomerOperationTime(String operation, long durationMs) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("customer.operation.duration")
                .tag("operation", operation)
                .register(meterRegistry));
    }

    /**
     * Increment API error counter
     */
    public void incrementApiError(String endpoint, String errorType) {
        Counter.builder("api.errors")
                .tag("endpoint", endpoint)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record custom gauge metric
     */
    public void recordGauge(String name, String description, double value, String... tags) {
        // Convert tags array to Tag objects
        io.micrometer.core.instrument.Tag[] tagArray = new io.micrometer.core.instrument.Tag[tags.length / 2];
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                tagArray[i / 2] = io.micrometer.core.instrument.Tag.of(tags[i], tags[i + 1]);
            }
        }
        meterRegistry.gauge(name, java.util.Arrays.asList(tagArray), value, v -> v);
    }

    /**
     * Record custom counter metric
     */
    public void incrementCounter(String name, String description, String... tags) {
        Counter.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record custom timer metric
     */
    public void recordTimer(String name, String description, long durationMs, String... tags) {
        Timer.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}