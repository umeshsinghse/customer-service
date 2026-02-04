# AWS CloudWatch Logging Integration

## Overview

This document describes the comprehensive AWS CloudWatch logging integration implemented for the Customer Service application. The integration provides structured logging, metrics collection, and monitoring capabilities for all API endpoints.

## 🏗️ Architecture

### Logging Structure
- **Application Logs**: General application events and business logic
- **API Access Logs**: HTTP request/response tracking with performance metrics
- **Error Logs**: Dedicated error tracking with stack traces and context
- **Health Check Logs**: System health monitoring and component status

### CloudWatch Log Groups
- `/aws/eks/customer-service/application` - Application and business logic logs
- `/aws/eks/customer-service/api-access` - API request/response logs
- `/aws/eks/customer-service/errors` - Error logs with extended retention

### Metrics Namespace
- `CustomerService/Application` - Custom application metrics
- `CustomerService/Metrics` - Micrometer-based system metrics

## 🔧 Components

### 1. Dependencies Added
```xml
<!-- AWS CloudWatch dependencies -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>cloudwatch</artifactId>
    <version>2.20.162</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>cloudwatchlogs</artifactId>
    <version>2.20.162</version>
</dependency>
<dependency>
    <groupId>ca.pjer</groupId>
    <artifactId>logback-awslogs-appender</artifactId>
    <version>1.6.0</version>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-cloudwatch2</artifactId>
</dependency>
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter</artifactId>
    <version>3.0.3</version>
</dependency>
```

### 2. Configuration Files

#### logback-spring.xml
- Configures CloudWatch appenders for different log types
- Async logging for performance optimization
- Profile-specific configurations (local vs AWS)
- Structured logging with MDC context

#### CloudWatchConfig.java
- AWS CloudWatch client configuration
- Micrometer registry setup for metrics
- Common tags for all metrics
- Environment-specific settings

### 3. Service Classes

#### LoggingService.java
- Structured logging with MDC (Mapped Diagnostic Context)
- API request logging with performance metrics
- Business event logging for CRUD operations
- Error logging with contextual information
- Health check logging

#### MetricsService.java
- Custom CloudWatch metrics publishing
- API operation counters
- Performance timers
- Error tracking metrics
- Custom gauge and counter methods

### 4. Interceptor

#### LoggingInterceptor.java
- Automatic API request/response logging
- Response time calculation
- Client IP extraction
- Customer ID extraction from URLs
- Error context capture

### 5. Health Monitoring

#### CloudWatchHealthIndicator.java
- CloudWatch connectivity health checks
- Integration with Spring Boot Actuator
- Automatic health status reporting

## 📊 Logged Information

### API Requests
- HTTP method and endpoint
- Client IP address
- Customer ID (when applicable)
- Response time in milliseconds
- HTTP status code
- Request ID for tracing

### Business Events
- CRUD operation type (CREATE, READ, UPDATE, DELETE)
- Entity type and ID
- Operation success/failure
- Detailed event description
- Event ID for tracking

### Performance Metrics
- Operation execution time
- API response times
- Customer operation counters
- Error rates and types
- System resource usage

### Error Information
- Error type and message
- Stack trace
- Operation context
- Request parameters
- Error ID for tracking

## 🚀 Usage

### Automatic Logging
Most logging is automatic through:
- Method-level logging in controllers and services
- Interceptor-based API request logging
- Exception handling with context capture

### Manual Logging
```java
@Autowired
private LoggingService loggingService;

// Log business event
loggingService.logBusinessEvent("CREATE", "Customer", customerId, 
    "Customer created successfully", true);

// Log performance metric
loggingService.logPerformanceMetric("customOperation", executionTime, 
    Map.of("param1", value1, "param2", value2));

// Log error with context
loggingService.logError("operationName", "Error message", exception, 
    Map.of("context1", value1));
```

### Custom Metrics
```java
@Autowired
private MetricsService metricsService;

// Increment operation counter
metricsService.incrementCustomerOperation("create");

// Record response time
metricsService.recordApiResponseTime(responseTimeMs);

// Custom counter
metricsService.incrementCounter("custom.metric", "Description", "tag1", "value1");
```

## 🔧 Configuration

### Environment Profiles

#### AWS Profile (application-aws.properties)
- Enables CloudWatch logging and metrics
- Configures log groups and retention
- Sets up AWS SDK parameters

#### Local Profile (application-local.properties)
- Disables CloudWatch integration
- Enhanced console logging
- Reduced AWS SDK noise

### Activation
```bash
# For AWS deployment
-Dspring.profiles.active=aws

# For local development
-Dspring.profiles.active=local
```

## 📈 CloudWatch Dashboard

### Key Metrics to Monitor
1. **API Performance**
   - Average response time
   - Request rate (requests/minute)
   - Error rate percentage

2. **Business Operations**
   - Customer CRUD operation counts
   - Success/failure rates
   - Operation duration trends

3. **System Health**
   - Application availability
   - CloudWatch connectivity
   - Error frequency and types

4. **Infrastructure**
   - Memory usage
   - CPU utilization
   - Network I/O

### Sample CloudWatch Queries
```sql
-- API Error Rate
fields @timestamp, @message
| filter @logGroup like /api-access/
| filter statusCode >= 400
| stats count() by bin(5m)

-- Average Response Time
fields @timestamp, responseTime
| filter @logGroup like /api-access/
| stats avg(responseTime) by bin(5m)

-- Business Operation Success Rate
fields @timestamp, operation, success
| filter @logGroup like /application/
| filter operation exists
| stats count() by operation, success
```

## 🔐 Security & Compliance

### IAM Permissions Required
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "logs:DescribeLogGroups",
                "logs:DescribeLogStreams"
            ],
            "Resource": "arn:aws:logs:us-east-1:*:log-group:/aws/eks/customer-service/*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "cloudwatch:PutMetricData"
            ],
            "Resource": "*"
        }
    ]
}
```

### Data Retention
- Application logs: 30 days
- API access logs: 30 days
- Error logs: 90 days (extended for compliance)

### PII Handling
- Customer emails and sensitive data are not logged
- Only customer IDs and non-sensitive metadata are included
- Request/response bodies are not logged to prevent data exposure

## 🛠️ Troubleshooting

### Common Issues

1. **CloudWatch Connectivity**
   - Check IAM permissions
   - Verify AWS region configuration
   - Test network connectivity to CloudWatch endpoints

2. **Missing Logs**
   - Verify log group creation
   - Check application profile activation
   - Review logback configuration

3. **Performance Impact**
   - Async appenders are used to minimize impact
   - Adjust batch sizes and flush intervals
   - Monitor application memory usage

### Debug Commands
```bash
# Check CloudWatch log groups
aws logs describe-log-groups --log-group-name-prefix "/aws/eks/customer-service"

# View recent log events
aws logs filter-log-events --log-group-name "/aws/eks/customer-service/application" --start-time 1640995200000

# Test application health
curl http://localhost:8080/api/health
```

## 📚 Additional Resources

- [AWS CloudWatch Logs Documentation](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/)
- [Micrometer CloudWatch Registry](https://micrometer.io/docs/registry/cloudwatch)
- [Logback AWS Appender](https://github.com/pierredavidbelanger/logback-awslogs-appender)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🔄 Maintenance

### Regular Tasks
1. Monitor log retention and costs
2. Review and optimize metric collection
3. Update CloudWatch dashboards and alarms
4. Rotate AWS credentials if using access keys
5. Review and clean up old log groups

### Performance Optimization
1. Adjust batch sizes based on throughput
2. Fine-tune flush intervals
3. Monitor memory usage of logging components
4. Consider log sampling for high-volume endpoints