# ECR Module

This module creates Amazon Elastic Container Registry (ECR) repositories for Spring Boot microservices with comprehensive lifecycle policies, security scanning, and access controls.

## Features

- **Private ECR Repositories**: Secure container image storage
- **Lifecycle Policies**: Automatic image cleanup and retention
- **Image Scanning**: Vulnerability scanning on push
- **Encryption**: AES256 or KMS encryption support
- **Access Policies**: Fine-grained repository access control
- **Replication**: Cross-region repository replication (optional)
- **Public Repositories**: ECR Public repository support (optional)
- **Registry Scanning**: Enhanced security scanning configuration

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Amazon ECR Repositories                   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │               customer-service                │    │
│  │                                                 │    │
│  │  • Image Scanning: ON_PUSH                    │    │
│  │  • Encryption: AES256/KMS                    │    │
│  │  • Lifecycle: 10 images, 30 days            │    │
│  │  • Access: IAM-based policies                │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                order-service                 │    │
│  │                                                 │    │
│  │  • Image Scanning: ON_PUSH                    │    │
│  │  • Encryption: AES256/KMS                    │    │
│  │  • Lifecycle: 10 images, 30 days            │    │
│  │  • Access: IAM-based policies                │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │               payment-service                │    │
│  │                                                 │    │
│  │  • Image Scanning: ON_PUSH                    │    │
│  │  • Encryption: AES256/KMS                    │    │
│  │  • Lifecycle: 10 images, 30 days            │    │
│  │  • Access: IAM-based policies                │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              inventory-service               │    │
│  │                                                 │    │
│  │  • Image Scanning: ON_PUSH                    │    │
│  │  • Encryption: AES256/KMS                    │    │
│  │  • Lifecycle: 10 images, 30 days            │    │
│  │  • Access: IAM-based policies                │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## Usage

```hcl
module "ecr" {
  source = "./modules/ecr"

  project_name    = "my-project"
  environment     = "dev"
  repository_names = [
    "customer-service",
    "order-service",
    "payment-service",
    "inventory-service"
  ]
  
  # Security settings
  scan_on_push         = true
  encryption_type      = "AES256"
  image_tag_mutability = "MUTABLE"
  
  # Lifecycle settings
  max_image_count      = 10
  image_retention_days = 30
  max_untagged_images  = 3
}
```

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|----------|
| project_name | Name of the project | `string` | n/a | yes |
| environment | Environment name | `string` | n/a | yes |
| repository_names | List of ECR repository names | `list(string)` | `["customer-service", ...]` | no |
| image_tag_mutability | Tag mutability setting | `string` | `"MUTABLE"` | no |
| scan_on_push | Enable image scanning on push | `bool` | `true` | no |
| encryption_type | Encryption type (AES256/KMS) | `string` | `"AES256"` | no |
| max_image_count | Maximum images to keep | `number` | `10` | no |
| image_retention_days | Days to retain images | `number` | `30` | no |

## Outputs

| Name | Description |
|------|-------------|
| repository_urls | Map of repository names to URLs |
| repository_arns | Map of repository names to ARNs |
| docker_login_command | Command to login to ECR |
| docker_commands | Build and push commands for each service |
| k8s_image_pull_secret | Kubernetes image pull secret command |

## Lifecycle Policies

The module automatically creates lifecycle policies with the following rules:

1. **Tagged Images**: Keep last N images (configurable)
2. **Time-based Retention**: Keep images for N days (configurable)
3. **Untagged Images**: Delete untagged images older than 1 day
4. **Untagged Limit**: Keep only latest N untagged images

### Example Lifecycle Policy

```json
{
  "rules": [
    {
      "rulePriority": 1,
      "description": "Keep last 10 images",
      "selection": {
        "tagStatus": "tagged",
        "tagPrefixList": ["v"],
        "countType": "imageCountMoreThan",
        "countNumber": 10
      },
      "action": {
        "type": "expire"
      }
    }
  ]
}
```

## Security Features

### Image Scanning
- **Scan on Push**: Automatic vulnerability scanning
- **Enhanced Scanning**: Advanced threat detection
- **Scan Results**: Integration with AWS Security Hub

### Encryption
- **AES256**: Server-side encryption with AWS managed keys
- **KMS**: Customer managed key encryption
- **In-transit**: HTTPS for all API calls

### Access Control
- **IAM Policies**: Fine-grained repository access
- **Cross-account Access**: Support for multi-account setups
- **Service-specific Permissions**: Separate push/pull permissions

## Docker Workflow

### 1. Login to ECR
```bash
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-west-2.amazonaws.com
```

### 2. Build and Tag Image
```bash
docker build -t customer-service .
docker tag customer-service:latest 123456789012.dkr.ecr.us-west-2.amazonaws.com/my-project-dev-customer-service:latest
```

### 3. Push Image
```bash
docker push 123456789012.dkr.ecr.us-west-2.amazonaws.com/my-project-dev-customer-service:latest
```

## Kubernetes Integration

### Image Pull Secret
```bash
kubectl create secret docker-registry ecr-secret \
  --docker-server=123456789012.dkr.ecr.us-west-2.amazonaws.com \
  --docker-username=AWS \
  --docker-password=$(aws ecr get-login-password --region us-west-2)
```

### Deployment Example
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: customer-service
spec:
  template:
    spec:
      imagePullSecrets:
      - name: ecr-secret
      containers:
      - name: customer-service
        image: 123456789012.dkr.ecr.us-west-2.amazonaws.com/my-project-dev-customer-service:latest
```

## Cost Optimization

1. **Lifecycle Policies**: Automatic cleanup of old images
2. **Compression**: Use multi-stage Docker builds
3. **Base Images**: Use minimal base images (Alpine, Distroless)
4. **Image Scanning**: Early vulnerability detection

## Best Practices

1. **Tagging Strategy**: Use semantic versioning (v1.0.0)
2. **Image Scanning**: Enable for all repositories
3. **Lifecycle Policies**: Configure appropriate retention
4. **Access Control**: Use least privilege IAM policies
5. **Encryption**: Use KMS for sensitive workloads
6. **Monitoring**: Set up CloudWatch alarms for repository events

## Troubleshooting

### Common Issues

1. **Authentication Failed**:
   ```bash
   aws ecr get-login-password --region us-west-2
   ```

2. **Repository Not Found**:
   ```bash
   aws ecr describe-repositories --region us-west-2
   ```

3. **Push Denied**:
   - Check IAM permissions
   - Verify repository policy

4. **Image Scan Failed**:
   - Check image format compatibility
   - Verify scanning configuration

### Validation Commands

```bash
# List repositories
aws ecr describe-repositories

# Check lifecycle policy
aws ecr get-lifecycle-policy --repository-name my-repo

# View scan results
aws ecr describe-image-scan-findings --repository-name my-repo --image-id imageTag=latest

# Check repository policy
aws ecr get-repository-policy --repository-name my-repo
```