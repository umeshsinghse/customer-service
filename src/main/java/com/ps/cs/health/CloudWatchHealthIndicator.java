package com.ps.cs.health;

import com.ps.cs.service.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;

import java.util.Map;
import java.util.HashMap;

/**
 * Health indicator for CloudWatch connectivity.
 * Checks if the application can connect to AWS CloudWatch Logs.
 */
@Component
public class CloudWatchHealthIndicator {

    private final LoggingService loggingService;

    @Autowired
    public CloudWatchHealthIndicator(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Check CloudWatch connectivity and return status
     */
    public Map<String, Object> checkCloudWatchHealth() {
        Map<String, Object> healthStatus = new HashMap<>();

        try {
            // Test CloudWatch connectivity by attempting to describe log groups
            CloudWatchLogsClient client = CloudWatchLogsClient.builder().build();

            DescribeLogGroupsRequest request = DescribeLogGroupsRequest.builder()
                    .logGroupNamePrefix("/aws/eks/customer-service")
                    .limit(1)
                    .build();

            DescribeLogGroupsResponse response = client.describeLogGroups(request);

            loggingService.logHealthCheck("cloudwatch-logs", true,
                    "Successfully connected to CloudWatch Logs");

            healthStatus.put("service", "CloudWatch Logs");
            healthStatus.put("status", "UP");
            healthStatus.put("connected", true);
            healthStatus.put("logGroups", response.logGroups().size());

            return healthStatus;

        } catch (Exception e) {
            loggingService.logHealthCheck("cloudwatch-logs", false,
                    "Failed to connect to CloudWatch Logs: " + e.getMessage());

            healthStatus.put("service", "CloudWatch Logs");
            healthStatus.put("status", "DOWN");
            healthStatus.put("connected", false);
            healthStatus.put("error", e.getMessage());

            return healthStatus;
        }
    }
}