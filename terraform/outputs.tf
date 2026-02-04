# Terraform Outputs

output "cluster_endpoint" {
  description = "Endpoint for EKS control plane"
  value       = module.eks.cluster_endpoint
  sensitive   = true
}

output "cluster_security_group_id" {
  description = "Security group ids attached to the cluster control plane"
  value       = module.eks.cluster_security_group_id
}

output "cluster_iam_role_name" {
  description = "IAM role name associated with EKS cluster"
  value       = module.eks.cluster_iam_role_name
}

output "cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data required to communicate with the cluster"
  value       = module.eks.cluster_certificate_authority_data
  sensitive   = true
}

output "cluster_name" {
  description = "The name of the EKS cluster"
  value       = module.eks.cluster_name
}

output "cluster_oidc_issuer_url" {
  description = "The URL on the EKS cluster for the OpenID Connect identity provider"
  value       = module.eks.cluster_oidc_issuer_url
}

output "node_groups" {
  description = "EKS node groups"
  value       = module.eks.eks_managed_node_groups
  sensitive   = true
}

output "vpc_id" {
  description = "ID of the VPC where the cluster and workers are deployed"
  value       = module.vpc.vpc_id
}

output "vpc_cidr_block" {
  description = "The CIDR block of the VPC"
  value       = module.vpc.vpc_cidr_block
}

output "private_subnets" {
  description = "List of IDs of private subnets"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "List of IDs of public subnets"
  value       = module.vpc.public_subnets
}

output "ecr_repository_url" {
  description = "The URL of the ECR repository"
  value       = aws_ecr_repository.customer_service.repository_url
}

output "ecr_repository_arn" {
  description = "The ARN of the ECR repository"
  value       = aws_ecr_repository.customer_service.arn
}

output "github_oidc_role_arn" {
  description = "ARN of the GitHub OIDC role for CI/CD"
  value       = module.github_oidc_role.iam_role_arn
}

output "cluster_autoscaler_role_arn" {
  description = "ARN of the cluster autoscaler IAM role"
  value       = var.enable_cluster_autoscaler ? aws_iam_role.cluster_autoscaler[0].arn : null
}

output "aws_load_balancer_controller_role_arn" {
  description = "ARN of the AWS Load Balancer Controller IAM role"
  value       = var.enable_aws_load_balancer_controller ? aws_iam_role.aws_load_balancer_controller[0].arn : null
}

output "cloudwatch_log_group_names" {
  description = "Names of CloudWatch log groups"
  value = var.enable_cloudwatch_logging ? {
    cluster         = aws_cloudwatch_log_group.eks_cluster[0].name
    customer_service = aws_cloudwatch_log_group.customer_service[0].name
    fluent_bit      = aws_cloudwatch_log_group.fluent_bit[0].name
  } : {}
}

output "sns_topic_arn" {
  description = "ARN of the SNS topic for alerts"
  value       = aws_sns_topic.alerts.arn
}

output "kms_key_arn" {
  description = "ARN of the KMS key for EKS encryption"
  value       = aws_kms_key.eks.arn
}

output "cloudwatch_kms_key_arn" {
  description = "ARN of the KMS key for CloudWatch logs encryption"
  value       = aws_kms_key.cloudwatch.arn
}

# Instructions for connecting to the cluster
output "kubectl_config_command" {
  description = "Command to configure kubectl"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
}

# GitHub Actions secrets that need to be configured
output "github_secrets" {
  description = "GitHub secrets that need to be configured"
  value = {
    AWS_REGION           = var.aws_region
    AWS_ACCOUNT_ID       = data.aws_caller_identity.current.account_id
    ECR_REPOSITORY       = aws_ecr_repository.customer_service.repository_url
    EKS_CLUSTER_NAME     = module.eks.cluster_name
    GITHUB_ROLE_ARN      = module.github_oidc_role.iam_role_arn
  }
}

# Verification commands
output "verification_commands" {
  description = "Commands to verify the deployment"
  value = {
    configure_kubectl = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
    check_nodes      = "kubectl get nodes"
    check_pods       = "kubectl get pods -A"
    check_services   = "kubectl get svc -A"
    test_ecr_access  = "aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${aws_ecr_repository.customer_service.repository_url}"
  }
}