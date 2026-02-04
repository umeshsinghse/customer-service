# Customer Service EKS Infrastructure

This Terraform configuration creates the AWS infrastructure required for the Customer Service application deployment on Amazon EKS, based on the CI/CD pipeline requirements.

## Architecture Overview

The infrastructure includes:
- **EKS Cluster**: Kubernetes cluster with managed node groups
- **VPC**: Custom VPC with public and private subnets across 3 AZs
- **ECR Repository**: Container registry for Docker images
- **IAM Roles**: GitHub OIDC integration for CI/CD
- **CloudWatch**: Logging and monitoring
- **Load Balancing**: AWS Load Balancer Controller
- **Auto Scaling**: Cluster Autoscaler and HPA support
- **Security**: KMS encryption, security groups, and network policies

## Prerequisites

1. **AWS CLI** configured with appropriate permissions
2. **Terraform** >= 1.0 installed
3. **kubectl** installed for cluster management
4. **GitHub repository** set up for CI/CD

## Quick Start

### 1. Clone and Configure

```bash
git clone <repository-url>
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

### 2. Customize Variables

Edit `terraform.tfvars` with your specific values:

```hcl
# Basic Configuration
aws_region      = "us-east-1"
environment     = "prod"
cluster_name    = "customer-service-cluster"

# GitHub Configuration
github_repository = "your-org/customer-service"

# Network Security (restrict in production)
allowed_cidr_blocks = ["YOUR_IP/32"]
```

### 3. Deploy Infrastructure

```bash
# Initialize Terraform
terraform init

# Plan deployment
terraform plan

# Apply configuration
terraform apply
```

### 4. Configure kubectl

```bash
# Configure kubectl to connect to the cluster
aws eks update-kubeconfig --region us-east-1 --name customer-service-cluster

# Verify connection
kubectl get nodes
kubectl get pods -A
```

### 5. Configure GitHub Secrets

Add these secrets to your GitHub repository:

```bash
# Get the values from Terraform outputs
terraform output github_secrets
```

Required GitHub Secrets:
- `AWS_ACCESS_KEY_ID`: Not needed (using OIDC)
- `AWS_SECRET_ACCESS_KEY`: Not needed (using OIDC)
- `AWS_REGION`: us-east-1
- `ECR_REPOSITORY`: customer-service
- `EKS_CLUSTER_NAME`: customer-service-cluster

## Infrastructure Components

### EKS Cluster
- **Version**: 1.29
- **Node Groups**: Managed node groups with auto-scaling
- **Instance Types**: t3.medium, t3.large
- **Capacity**: 1-10 nodes (3 desired)
- **Networking**: Private subnets with NAT Gateway access

### Security Features
- **Encryption**: EKS secrets encrypted with KMS
- **Network**: Security groups with minimal required access
- **IAM**: Least privilege roles for GitHub Actions and cluster components
- **Logging**: CloudWatch integration for audit and application logs

### Monitoring & Observability
- **CloudWatch Logs**: Centralized logging for cluster and applications
- **CloudWatch Dashboard**: Pre-configured dashboard for monitoring
- **Metrics Server**: Kubernetes metrics for HPA
- **Alarms**: CPU and memory utilization alerts

### Add-ons Installed
- **AWS Load Balancer Controller**: For LoadBalancer services
- **Cluster Autoscaler**: Automatic node scaling
- **Metrics Server**: Required for HPA
- **AWS for Fluent Bit**: Log shipping to CloudWatch

## Usage Examples

### Deploy Application

```bash
# Create namespace
kubectl create namespace customer-service

# Apply ConfigMap (created by Terraform)
kubectl get configmap customer-service-config -n customer-service

# Deploy application (via CI/CD or manually)
kubectl apply -f k8s/deployment.yaml -n customer-service
```

### Monitor Deployment

```bash
# Check deployment status
kubectl get deployments -n customer-service
kubectl get pods -n customer-service
kubectl get svc -n customer-service

# Check HPA status
kubectl get hpa -n customer-service

# View logs
kubectl logs -f deployment/customer-service-deployment -n customer-service
```

### Access Application

```bash
# Port forward for local testing
kubectl port-forward svc/customer-service-service 8080:80 -n customer-service

# Test health endpoint
curl http://localhost:8080/api/health

# Get LoadBalancer endpoint
kubectl get svc customer-service-service -n customer-service
```

## Troubleshooting

### Common Issues

1. **LoadBalancer Pending**
   ```bash
   # Check subnet tags
   aws ec2 describe-subnets --subnet-ids $(terraform output -json public_subnets | jq -r '.[]')
   
   # Verify AWS Load Balancer Controller
   kubectl get pods -n kube-system -l app.kubernetes.io/name=aws-load-balancer-controller
   ```

2. **Node Group Issues**
   ```bash
   # Check node group status
   aws eks describe-nodegroup --cluster-name customer-service-cluster --nodegroup-name customer-service-nodes
   
   # Check cluster autoscaler logs
   kubectl logs -n kube-system deployment/cluster-autoscaler
   ```

3. **Pod Startup Issues**
   ```bash
   # Check pod events
   kubectl describe pod <pod-name> -n customer-service
   
   # Check resource quotas
   kubectl describe nodes
   kubectl top nodes
   ```

### Useful Commands

```bash
# Cluster information
kubectl cluster-info
kubectl get nodes -o wide

# Check all resources in customer-service namespace
kubectl get all -n customer-service

# View cluster events
kubectl get events -A --sort-by='.lastTimestamp'

# Check resource usage
kubectl top nodes
kubectl top pods -A
```

## Security Considerations

### Production Hardening

1. **Network Security**
   - Restrict `allowed_cidr_blocks` to your IP ranges
   - Use private endpoints for EKS API server
   - Implement network policies

2. **IAM Security**
   - Review and minimize IAM permissions
   - Enable CloudTrail for API logging
   - Use separate roles for different environments

3. **Encryption**
   - All data encrypted at rest (EBS, EKS secrets)
   - CloudWatch logs encrypted with KMS
   - Consider envelope encryption for application secrets

4. **Monitoring**
   - Enable GuardDuty for threat detection
   - Configure CloudWatch alarms for security events
   - Regular security scanning of container images

## Cost Optimization

1. **Instance Types**: Use Spot instances for non-production
2. **Auto Scaling**: Configure appropriate scaling policies
3. **Resource Limits**: Set resource requests and limits
4. **Log Retention**: Adjust log retention periods
5. **Unused Resources**: Regular cleanup of unused resources

## Maintenance

### Regular Tasks

1. **Cluster Updates**
   ```bash
   # Update cluster version
   terraform plan -var="cluster_version=1.30"
   terraform apply
   ```

2. **Node Group Updates**
   ```bash
   # Update node group AMI
   aws eks update-nodegroup-version --cluster-name customer-service-cluster --nodegroup-name customer-service-nodes
   ```

3. **Add-on Updates**
   ```bash
   # Update Helm charts
   helm repo update
   helm upgrade aws-load-balancer-controller eks/aws-load-balancer-controller -n kube-system
   ```

## Disaster Recovery

1. **Backup Strategy**
   - EKS cluster configuration in Terraform
   - Application manifests in Git
   - Persistent data backup (if applicable)

2. **Recovery Procedure**
   - Restore infrastructure with Terraform
   - Redeploy applications via CI/CD
   - Restore data from backups

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review AWS EKS documentation
3. Check Terraform module documentation
4. Contact the platform team

## Terraform Outputs

After successful deployment, important outputs are available:

```bash
# View all outputs
terraform output

# Specific outputs
terraform output cluster_endpoint
terraform output ecr_repository_url
terraform output github_secrets
```

These outputs contain the information needed to configure your CI/CD pipeline and connect to the cluster.