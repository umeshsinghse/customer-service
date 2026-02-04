package com.ps.cs.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

/**
 * Configuration for AWS CloudWatch integration.
 * Configures metrics publishing and custom dimensions.
 */
@Configuration
@Profile("!local")
public class AwsCloudWatchConfig {

    @Value("${spring.application.name:customer-service}")
    private String applicationName;

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.cloudwatch.namespace:CustomerService/Application}")
    private String cloudWatchNamespace;

    /**
     * Configure CloudWatch async client
     */
    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * Configure CloudWatch meter registry for metrics
     */
    @Bean
    public CloudWatchMeterRegistry cloudWatchMeterRegistry(CloudWatchAsyncClient cloudWatchAsyncClient) {
        io.micrometer.cloudwatch2.CloudWatchConfig cloudWatchConfig = new io.micrometer.cloudwatch2.CloudWatchConfig() {
            @Override
            public String get(String key) {
                return null; // Use default values
            }

            @Override
            public String namespace() {
                return cloudWatchNamespace;
            }

            @Override
            public Duration step() {
                return Duration.ofMinutes(1); // Send metrics every minute
            }

            @Override
            public int batchSize() {
                return 20; // Send up to 20 metrics per request
            }
        };

        return new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, cloudWatchAsyncClient);
    }

    /**
     * Add common tags to all metrics
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
                "application", applicationName,
                "environment", getEnvironment(),
                "region", awsRegion
        );
    }

    /**
     * Determine environment from active profiles or system properties
     */
    private String getEnvironment() {
        String env = System.getProperty("spring.profiles.active");
        if (env != null && !env.isEmpty()) {
            return env;
        }
        return System.getenv().getOrDefault("ENVIRONMENT", "unknown");
    }
}