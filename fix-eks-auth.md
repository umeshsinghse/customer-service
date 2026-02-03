# EKS Authentication Fix Guide

## Problem
Receiving "Unauthorized" error when accessing EKS cluster endpoint.

## Root Cause
1. Invalid or missing AWS credentials
2. kubectl not configured for EKS cluster
3. Missing IAM permissions for EKS access

## Solutions

### Step 1: Configure AWS Credentials

#### Option A: Using AWS CLI Configure
```bash
aws configure
```
Enter your:
- AWS Access Key ID
- AWS Secret Access Key  
- Default region: us-east-1
- Default output format: json

#### Option B: Using Environment Variables
```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_DEFAULT_REGION=us-east-1
```

#### Option C: Using AWS Profile
```bash
aws configure --profile your-profile-name
export AWS_PROFILE=your-profile-name
```

### Step 2: Verify AWS Credentials
```bash
# Test AWS credentials
aws sts get-caller-identity

# List EKS clusters
aws eks list-clusters --region us-east-1

# Describe your specific cluster
aws eks describe-cluster --name customer-service-cluster --region us-east-1
```

### Step 3: Update kubeconfig for EKS
```bash
# Update kubeconfig to connect to EKS cluster
aws eks update-kubeconfig --region us-east-1 --name customer-service-cluster

# Verify kubectl configuration
kubectl config current-context
kubectl cluster-info
```

### Step 4: Test EKS Access
```bash
# Test basic connectivity
kubectl get nodes
kubectl get namespaces

# Test access to your application namespace
kubectl get pods -n customer-service
kubectl get services -n customer-service
```

## IAM Permissions Required

Your AWS user/role needs these permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "eks:DescribeCluster",
                "eks:ListClusters",
                "eks:DescribeNodegroup",
                "eks:ListNodegroups",
                "eks:DescribeUpdate",
                "eks:ListUpdates"
            ],
            "Resource": "*"
        }
    ]
}
```

## Troubleshooting Commands

### Check AWS Configuration
```bash
# Check current AWS identity
aws sts get-caller-identity

# Check AWS configuration
aws configure list

# Check if cluster exists
aws eks describe-cluster --name customer-service-cluster --region us-east-1
```

### Check kubectl Configuration
```bash
# Check current context
kubectl config current-context

# List all contexts
kubectl config get-contexts

# Check cluster info
kubectl cluster-info

# Debug connection
kubectl cluster-info dump
```

### Check EKS Cluster Status
```bash
# Check cluster status
aws eks describe-cluster --name customer-service-cluster --region us-east-1 --query 'cluster.status'

# Check node groups
aws eks list-nodegroups --cluster-name customer-service-cluster --region us-east-1

# Check node group status
aws eks describe-nodegroup --cluster-name customer-service-cluster --nodegroup-name <nodegroup-name> --region us-east-1
```

## Common Issues and Fixes

### Issue 1: "aws command not found"
**Solution**: Install AWS CLI
```bash
# Windows (using chocolatey)
choco install awscli

# Or download from: https://aws.amazon.com/cli/
```

### Issue 2: "kubectl command not found"
**Solution**: Install kubectl
```bash
# Windows (using chocolatey)
choco install kubernetes-cli

# Or download from: https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
```

### Issue 3: "cluster not found"
**Solution**: Verify cluster name and region
```bash
aws eks list-clusters --region us-east-1
```

### Issue 4: "access denied"
**Solution**: Check IAM permissions and ensure your user has EKS access

### Issue 5: "token expired"
**Solution**: Refresh AWS credentials
```bash
# If using temporary credentials, get new ones
# If using permanent credentials, verify they're correct
aws configure list
```

## Testing the Fix

After applying the fixes, test with:

```bash
# Test 1: AWS connectivity
aws sts get-caller-identity

# Test 2: EKS cluster access
aws eks describe-cluster --name customer-service-cluster --region us-east-1

# Test 3: kubectl connectivity
kubectl cluster-info

# Test 4: Access your application
kubectl get pods -n customer-service
kubectl get services -n customer-service

# Test 5: Get service endpoint
kubectl get service customer-service-service -n customer-service
```

## Expected Success Output

After successful configuration, you should see:

```bash
$ kubectl cluster-info
Kubernetes control plane is running at https://YOUR-CLUSTER-ENDPOINT.gr7.us-east-1.eks.amazonaws.com
CoreDNS is running at https://YOUR-CLUSTER-ENDPOINT.gr7.us-east-1.eks.amazonaws.com/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

$ kubectl get nodes
NAME                                       STATUS   ROLES    AGE   VERSION
ip-10-0-1-100.us-east-1.compute.internal   Ready    <none>   1h    v1.29.0
```

## Next Steps

Once authentication is fixed:
1. Deploy your application using the CI/CD pipeline
2. Verify pods are running: `kubectl get pods -n customer-service`
3. Check service status: `kubectl get services -n customer-service`
4. Access your application through the LoadBalancer endpoint