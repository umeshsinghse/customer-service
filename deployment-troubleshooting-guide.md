# 🚨 Kubernetes Deployment Timeout - Complete Troubleshooting Guide

## Problem Analysis

Based on the error message and service description, there are **two critical issues**:

### 1. LoadBalancer Subnet Resolution Issue
```
Failed build model due to unable to resolve at least one subnet (0 match VPC and tags: [kubernetes.io/role/internal-elb])
```

### 2. Deployment Rollout Timeout
```
Waiting for deployment "customer-service-deployment" rollout to finish: 2 out of 3 new replicas have been updated...
error: timed out waiting for the condition
```

## Root Cause Analysis

### Issue 1: LoadBalancer Subnet Problem
- **Cause**: EKS cluster subnets are not properly tagged for LoadBalancer creation
- **Impact**: LoadBalancer service cannot provision external IP
- **Status**: Service shows `LoadBalancer endpoint is still pending...`

### Issue 2: Health Check Endpoint Mismatch
- **Cause**: Deployment uses `/actuator/health/liveness` and `/actuator/health/readiness` endpoints
- **Reality**: Application only exposes `/api/health` endpoint in CustomerController
- **Impact**: Pods fail health checks and never become ready

## 🔧 Complete Fix Implementation

### Step 1: Fix Health Check Endpoints

The deployment has been updated to use the correct health endpoint:

```yaml
# Before (WRONG)
livenessProbe:
  httpGet:
    path: /actuator/health/liveness  # This endpoint doesn't exist!
    port: 8080

# After (CORRECT)
livenessProbe:
  httpGet:
    path: /api/health  # This endpoint exists in CustomerController
    port: 8080
```

### Step 2: LoadBalancer Service Fix

Created separate LoadBalancer service with proper AWS annotations:

```yaml
# k8s/loadbalancer-fix.yaml
apiVersion: v1
kind: Service
metadata:
  name: customer-service-loadbalancer
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
    service.beta.kubernetes.io/aws-load-balancer-scheme: "internet-facing"
    # ... other AWS-specific annotations
```

### Step 3: JVM Optimization

Added JVM flags for faster startup:

```yaml
env:
  - name: JAVA_OPTS
    value: "-Xms256m -Xmx512m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
```

## 🚀 Deployment Instructions

### Option A: Apply Fixed Deployment (Recommended)

```bash
# Apply the updated deployment with correct health endpoints
kubectl apply -f k8s/deployment.yaml -n customer-service

# Monitor the rollout
kubectl rollout status deployment/customer-service-deployment -n customer-service --timeout=600s

# Check pod status
kubectl get pods -n customer-service -l app=customer-service
```

### Option B: Use Alternative Fixed Configuration

```bash
# Apply the complete fixed configuration
kubectl apply -f k8s/deployment-fix.yaml -n customer-service

# Apply the LoadBalancer fix
kubectl apply -f k8s/loadbalancer-fix.yaml -n customer-service
```

### Option C: Debug Mode (If issues persist)

```bash
# Use the debug deployment with extended timeouts
kubectl apply -f k8s/troubleshoot-deployment.yaml -n customer-service

# Monitor debug pod
kubectl logs -f deployment/customer-service-debug -n customer-service
```

## 🔍 Verification Steps

### 1. Check Pod Health
```bash
# Verify pods are running
kubectl get pods -n customer-service -l app=customer-service

# Check pod logs for startup issues
kubectl logs -f deployment/customer-service-deployment -n customer-service

# Describe pods for detailed status
kubectl describe pods -n customer-service -l app=customer-service
```

### 2. Test Health Endpoint
```bash
# Port-forward to test health endpoint directly
kubectl port-forward -n customer-service svc/customer-service-service 8080:80

# In another terminal, test the endpoint
curl http://localhost:8080/api/health
# Expected: {"status":"UP"}
```

### 3. Check Service Status
```bash
# Check service endpoints
kubectl get svc -n customer-service

# Describe LoadBalancer service
kubectl describe svc customer-service-loadbalancer -n customer-service

# Check endpoints
kubectl get endpoints -n customer-service
```

## 🛠️ AWS EKS Subnet Fix (If LoadBalancer still fails)

If the LoadBalancer continues to have subnet issues, you need to tag your subnets:

### For Public Subnets (Internet-facing LoadBalancers):
```bash
# Tag public subnets
aws ec2 create-tags --resources subnet-12345678 --tags Key=kubernetes.io/role/elb,Value=1
aws ec2 create-tags --resources subnet-87654321 --tags Key=kubernetes.io/role/elb,Value=1
```

### For Private Subnets (Internal LoadBalancers):
```bash
# Tag private subnets
aws ec2 create-tags --resources subnet-abcdef12 --tags Key=kubernetes.io/role/internal-elb,Value=1
aws ec2 create-tags --resources subnet-fedcba21 --tags Key=kubernetes.io/role/internal-elb,Value=1
```

### Find Your Subnet IDs:
```bash
# Get cluster VPC
VPC_ID=$(aws eks describe-cluster --name customer-service-cluster --query 'cluster.resourcesVpcConfig.vpcId' --output text)

# List subnets in the VPC
aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].{SubnetId:SubnetId,AvailabilityZone:AvailabilityZone,CidrBlock:CidrBlock}' --output table
```

## 🎯 Expected Results

After applying the fixes:

1. **Deployment Success**: All 3 replicas should be running and ready
2. **Health Checks Pass**: Pods respond to `/api/health` endpoint
3. **Service Available**: ClusterIP service works immediately
4. **LoadBalancer Ready**: External IP assigned (may take 2-5 minutes)
5. **Zero Downtime**: Rolling update completes without service interruption

## 📊 Monitoring Commands

```bash
# Watch deployment progress
watch kubectl get pods -n customer-service -l app=customer-service

# Monitor events
kubectl get events -n customer-service --sort-by='.lastTimestamp'

# Check resource usage
kubectl top pods -n customer-service

# View deployment status
kubectl get deployment customer-service-deployment -n customer-service -o wide
```

## 🚨 If Problems Persist

1. **Check EKS Cluster Status**: Ensure cluster and node groups are healthy
2. **Verify IAM Permissions**: LoadBalancer creation requires specific AWS permissions
3. **Resource Constraints**: Check if cluster has sufficient CPU/memory
4. **Network Policies**: Ensure no network policies block health check traffic
5. **Security Groups**: Verify security groups allow traffic on port 8080

The fixes address both the health check endpoint mismatch and provide a workaround for the LoadBalancer subnet issue. The deployment should now complete successfully within 10-15 minutes.