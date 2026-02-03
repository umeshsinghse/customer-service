# Kubernetes Deployment Timeout Fix Guide

## Problem Analysis

The deployment timeout error indicates that the Spring Boot application pods are failing to start properly within the allocated time. The main issues identified:

1. **Missing Actuator Dependency**: The deployment uses `/actuator/health/*` endpoints but the application lacks Spring Boot Actuator
2. **Aggressive Probe Timings**: Health check probes have insufficient startup time for Spring Boot
3. **Insufficient Resources**: Limited CPU/memory may cause slow startup
4. **Rolling Update Strategy**: `maxUnavailable: 1` can cause service disruption during updates

## Changes Made

### 1. Added Spring Boot Actuator
- **File**: `pom.xml`
- **Change**: Added `spring-boot-starter-actuator` dependency
- **Reason**: Enables `/actuator/health/*` endpoints required by Kubernetes probes

### 2. Increased Probe Timeouts
- **File**: `k8s/deployment.yaml`
- **Changes**:
  - `livenessProbe.initialDelaySeconds`: 60 → 120 seconds
  - `readinessProbe.initialDelaySeconds`: 30 → 60 seconds
  - `startupProbe.initialDelaySeconds`: 10 → 30 seconds
  - `startupProbe.failureThreshold`: 30 → 40 (10 minutes total)
- **Reason**: Spring Boot applications need more time to initialize

### 3. Increased Resource Limits
- **File**: `k8s/deployment.yaml`
- **Changes**:
  - Memory requests: 256Mi → 512Mi
  - Memory limits: 512Mi → 1Gi
  - CPU requests: 250m → 500m
  - CPU limits: 500m → 1000m
- **Reason**: Ensures sufficient resources for Spring Boot startup

### 4. Improved Rolling Update Strategy
- **File**: `k8s/deployment.yaml`
- **Change**: `maxUnavailable`: 1 → 0
- **Reason**: Prevents service downtime during updates

### 5. Optimized Docker Container
- **File**: `Dockerfile`
- **Change**: Added JVM optimization flags
- **Reason**: Faster startup and better memory management

### 6. Added Application Configuration
- **File**: `src/main/resources/application.properties`
- **Purpose**: Configure actuator endpoints and optimize startup

### 7. Created Debug Deployment
- **File**: `k8s/troubleshoot-deployment.yaml`
- **Purpose**: Simplified deployment for testing with extended timeouts

## Deployment Steps

### Option 1: Apply Fixed Configuration
```bash
# Rebuild application with actuator dependency
mvn clean package -DskipTests

# Rebuild and push Docker image
docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

# Apply updated deployment
kubectl apply -f k8s/deployment.yaml -n customer-service

# Monitor rollout
kubectl rollout status deployment/customer-service-deployment -n customer-service --timeout=600s
```

### Option 2: Use Debug Deployment
```bash
# Deploy debug version with extended timeouts
kubectl apply -f k8s/troubleshoot-deployment.yaml -n customer-service

# Monitor pods
kubectl get pods -n customer-service -l app=customer-service-debug -w

# Check logs if issues persist
kubectl logs -n customer-service -l app=customer-service-debug --tail=100
```

## Troubleshooting Commands

```bash
# Check pod status
kubectl get pods -n customer-service -l app=customer-service

# Describe problematic pods
kubectl describe pods -n customer-service -l app=customer-service

# Check pod logs
kubectl logs -n customer-service -l app=customer-service --tail=100

# Check events
kubectl get events -n customer-service --sort-by='.lastTimestamp'

# Test health endpoints manually
kubectl port-forward -n customer-service pod/<pod-name> 8080:8080
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/health
```

## Expected Results

After applying these fixes:
1. Pods should start successfully within 5-10 minutes
2. Health check endpoints will be available
3. Rolling updates will complete without service interruption
4. Deployment will scale properly to 3 replicas

## Prevention

- Always include actuator dependency for Spring Boot applications in Kubernetes
- Set realistic probe timeouts based on application startup time
- Allocate sufficient resources for JVM-based applications
- Test deployments in staging environment before production
- Monitor application startup metrics to optimize probe timings