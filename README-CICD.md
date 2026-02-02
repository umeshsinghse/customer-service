# CI/CD Setup for Customer Service

This document describes the CI/CD pipeline setup for the Customer Service Spring Boot application using GitHub Actions and AWS CodeBuild.

## Overview

The CI/CD pipeline consists of:
- **GitHub Actions**: Handles CI (testing, building, Docker image creation)
- **AWS CodeBuild**: Handles CD (deployment to staging and production)

## Pipeline Architecture

```
GitHub Repository
       ↓
   GitHub Actions
   (CI Pipeline)
       ↓
   AWS ECR
   (Container Registry)
       ↓
   AWS CodeBuild
   (CD Pipeline)
       ↓
   AWS ECS
   (Container Orchestration)
```

## GitHub Actions Workflow

### File: `.github/workflows/ci-cd.yml`

The workflow includes the following jobs:

1. **Test Job**
   - Runs on every push and pull request
   - Sets up Java 17 and Maven
   - Executes unit tests
   - Generates test reports

2. **Build Job**
   - Runs after successful tests on main/develop branches
   - Compiles and packages the application
   - Uploads JAR artifacts

3. **Docker Build Job**
   - Runs after successful build on main branch
   - Builds Docker image
   - Pushes to Amazon ECR

4. **Deploy Jobs**
   - **Staging**: Deploys develop branch to staging environment
   - **Production**: Deploys main branch to production environment
   - Triggers AWS CodeBuild projects for actual deployment

### Required GitHub Secrets

Add the following secrets to your GitHub repository:

```
AWS_ACCESS_KEY_ID       # AWS access key for ECR and CodeBuild
AWS_SECRET_ACCESS_KEY   # AWS secret key
```

## AWS CodeBuild Projects

### 1. Main Build Project (`buildspec.yml`)

**Purpose**: Build, test, and push Docker images

**Features**:
- Java 17 runtime
- Maven build and test
- Docker image creation
- ECR push
- Optional SonarQube analysis
- Security scanning

**Environment Variables**:
```
AWS_ACCOUNT_ID=123456789012  # Replace with your AWS Account ID
ECR_REPOSITORY=customer-service
AWS_DEFAULT_REGION=us-east-1
```

### 2. Staging Deployment (`buildspec-staging.yml`)

**Purpose**: Deploy to staging environment

**Features**:
- ECS service update
- Health checks
- Rollback on failure

**CodeBuild Project Name**: `customer-service-staging-deploy`

### 3. Production Deployment (`buildspec-production.yml`)

**Purpose**: Deploy to production environment

**Features**:
- Blue-green deployment strategy
- Enhanced health checks
- Automatic rollback
- Deployment notifications

**CodeBuild Project Name**: `customer-service-production-deploy`

## AWS Infrastructure Requirements

### 1. Amazon ECR Repository

Create an ECR repository:
```bash
aws ecr create-repository --repository-name customer-service --region us-east-1
```

### 2. ECS Clusters and Services

Create ECS clusters:
```bash
# Staging cluster
aws ecs create-cluster --cluster-name customer-service-staging

# Production cluster
aws ecs create-cluster --cluster-name customer-service-production
```

### 3. CodeBuild Projects

Create CodeBuild projects using the AWS Console or CLI:

#### Main Build Project
```bash
aws codebuild create-project \
  --name customer-service-build \
  --source type=GITHUB,location=https://github.com/your-org/customer-service.git \
  --artifacts type=S3,location=your-build-artifacts-bucket \
  --environment type=LINUX_CONTAINER,image=aws/codebuild/amazonlinux2-x86_64-standard:4.0,computeType=BUILD_GENERAL1_MEDIUM \
  --service-role arn:aws:iam::123456789012:role/CodeBuildServiceRole
```

#### Staging Deployment Project
```bash
aws codebuild create-project \
  --name customer-service-staging-deploy \
  --source type=GITHUB,location=https://github.com/your-org/customer-service.git,buildspec=buildspec-staging.yml \
  --environment type=LINUX_CONTAINER,image=aws/codebuild/amazonlinux2-x86_64-standard:4.0,computeType=BUILD_GENERAL1_SMALL \
  --service-role arn:aws:iam::123456789012:role/CodeBuildServiceRole
```

#### Production Deployment Project
```bash
aws codebuild create-project \
  --name customer-service-production-deploy \
  --source type=GITHUB,location=https://github.com/your-org/customer-service.git,buildspec=buildspec-production.yml \
  --environment type=LINUX_CONTAINER,image=aws/codebuild/amazonlinux2-x86_64-standard:4.0,computeType=BUILD_GENERAL1_SMALL \
  --service-role arn:aws:iam::123456789012:role/CodeBuildServiceRole
```

## IAM Roles and Permissions

### CodeBuild Service Role

Create a service role with the following policies:

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
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:GetAuthorizationToken",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecs:DescribeServices",
        "ecs:DescribeTaskDefinition",
        "ecs:RegisterTaskDefinition",
        "ecs:UpdateService",
        "ecs:DescribeTasks",
        "iam:PassRole",
        "ssm:GetParameters",
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "*"
    }
  ]
}
```

### GitHub Actions User

Create an IAM user for GitHub Actions with policies for:
- ECR access
- CodeBuild project execution
- Parameter Store/Secrets Manager access (if needed)

## Configuration

### Environment Variables

Update the following variables in the buildspec files:

1. **AWS_ACCOUNT_ID**: Your AWS account ID
2. **ECR_REPOSITORY**: Your ECR repository name
3. **ECS_CLUSTER_NAME**: Your ECS cluster names
4. **ECS_SERVICE_NAME**: Your ECS service names

### Parameter Store Configuration

Store environment-specific configuration in AWS Systems Manager Parameter Store:

```bash
# Staging parameters
aws ssm put-parameter --name "/customer-service/staging/db/host" --value "staging-db.example.com" --type "String"
aws ssm put-parameter --name "/customer-service/staging/db/password" --value "staging-password" --type "SecureString"

# Production parameters
aws ssm put-parameter --name "/customer-service/production/db/host" --value "prod-db.example.com" --type "String"
aws ssm put-parameter --name "/customer-service/production/db/password" --value "prod-password" --type "SecureString"
```

### Secrets Manager Configuration

Store sensitive values in AWS Secrets Manager:

```bash
# Staging secrets
aws secretsmanager create-secret --name "customer-service/staging" --description "Customer Service Staging Secrets" --secret-string '{"api_secret_key":"staging-secret-key"}'

# Production secrets
aws secretsmanager create-secret --name "customer-service/production" --description "Customer Service Production Secrets" --secret-string '{"api_secret_key":"production-secret-key"}'
```

## Deployment Flow

### Development Workflow

1. **Feature Development**
   ```bash
   git checkout -b feature/new-feature
   # Make changes
   git commit -m "Add new feature"
   git push origin feature/new-feature
   # Create pull request
   ```

2. **Pull Request**
   - GitHub Actions runs tests automatically
   - Code review and approval
   - Merge to develop branch

3. **Staging Deployment**
   ```bash
   git checkout develop
   git merge feature/new-feature
   git push origin develop
   # Automatic deployment to staging
   ```

4. **Production Deployment**
   ```bash
   git checkout main
   git merge develop
   git push origin main
   # Automatic deployment to production
   ```

### Manual Deployment

To trigger manual deployments:

```bash
# Trigger staging deployment
aws codebuild start-build --project-name customer-service-staging-deploy --source-version develop

# Trigger production deployment
aws codebuild start-build --project-name customer-service-production-deploy --source-version main
```

## Monitoring and Troubleshooting

### GitHub Actions

- View workflow runs in the "Actions" tab of your GitHub repository
- Check logs for each job step
- Monitor artifact uploads and downloads

### AWS CodeBuild

- View build history in the AWS CodeBuild console
- Check CloudWatch logs for detailed build output
- Monitor build metrics and duration

### Common Issues

1. **ECR Authentication Failures**
   - Verify AWS credentials in GitHub secrets
   - Check IAM permissions for ECR access

2. **ECS Deployment Failures**
   - Verify ECS cluster and service names
   - Check task definition compatibility
   - Monitor ECS service events

3. **Build Failures**
   - Check Maven dependencies and versions
   - Verify Java version compatibility
   - Review test failures in reports

## Security Best Practices

1. **Use least privilege IAM policies**
2. **Store secrets in AWS Secrets Manager**
3. **Use Parameter Store for configuration**
4. **Enable container image scanning**
5. **Implement security scanning in build pipeline**
6. **Use environment-specific configurations**
7. **Enable CloudTrail for audit logging**

## Next Steps

1. Set up monitoring and alerting with CloudWatch
2. Implement automated testing strategies
3. Add performance testing to the pipeline
4. Set up disaster recovery procedures
5. Implement infrastructure as code (IaC) with CloudFormation or Terraform

## Support

For issues or questions regarding the CI/CD pipeline:

1. Check the troubleshooting section above
2. Review AWS CodeBuild and GitHub Actions documentation
3. Contact the DevOps team for infrastructure-related issues