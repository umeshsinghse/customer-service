# EKS Cluster Configuration

module "eks" {
  source = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = var.cluster_name
  cluster_version = var.cluster_version

  vpc_id                         = module.vpc.vpc_id
  subnet_ids                     = module.vpc.private_subnets
  cluster_endpoint_public_access = true
  cluster_endpoint_private_access = true
  cluster_endpoint_public_access_cidrs = var.allowed_cidr_blocks

  # Cluster security group
  cluster_additional_security_group_ids = [aws_security_group.additional.id]

  # EKS Managed Node Group(s)
  eks_managed_node_group_defaults = {
    instance_types = var.node_group_instance_types
    
    # Disk configuration
    disk_size = 50
    disk_type = "gp3"
    disk_encrypted = true
    
    # AMI configuration
    ami_type = "AL2_x86_64"
    capacity_type = "ON_DEMAND"
    
    # Network configuration
    subnet_ids = module.vpc.private_subnets
    
    # Security groups
    vpc_security_group_ids = [aws_security_group.additional.id]
    
    # IAM role configuration
    iam_role_attach_cni_policy = true
    
    # User data
    enable_bootstrap_user_data = true
    
    # Tags
    tags = var.tags
  }

  eks_managed_node_groups = {
    customer_service_nodes = {
      name = "customer-service-nodes"
      
      min_size     = var.node_group_min_size
      max_size     = var.node_group_max_size
      desired_size = var.node_group_desired_size

      instance_types = var.node_group_instance_types
      capacity_type  = "ON_DEMAND"

      # Launch template configuration
      create_launch_template = false
      launch_template_name   = ""
      
      # Scaling configuration
      update_config = {
        max_unavailable_percentage = 33
      }
      
      # Labels
      labels = {
        Environment = var.environment
        NodeGroup   = "customer-service-nodes"
      }
      
      # Taints
      taints = {}
      
      tags = merge(var.tags, {
        "k8s.io/cluster-autoscaler/enabled" = var.enable_cluster_autoscaler
        "k8s.io/cluster-autoscaler/${var.cluster_name}" = "owned"
      })
    }
  }

  # Cluster access entry
  # To add the current caller identity as an administrator
  enable_cluster_creator_admin_permissions = true
  
  # aws-auth configmap
  manage_aws_auth_configmap = true

  aws_auth_roles = [
    {
      rolearn  = module.github_oidc_role.iam_role_arn
      username = "github-actions"
      groups   = ["system:masters"]
    },
  ]

  # Cluster logging
  cluster_enabled_log_types = var.enable_cloudwatch_logging ? [
    "api",
    "audit",
    "authenticator",
    "controllerManager",
    "scheduler"
  ] : []

  # Cluster encryption
  cluster_encryption_config = {
    provider_key_arn = aws_kms_key.eks.arn
    resources        = ["secrets"]
  }

  tags = var.tags
}

# KMS key for EKS cluster encryption
resource "aws_kms_key" "eks" {
  description             = "EKS Secret Encryption Key for ${var.cluster_name}"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = merge(var.tags, {
    Name = "${var.cluster_name}-eks-encryption-key"
  })
}

resource "aws_kms_alias" "eks" {
  name          = "alias/${var.cluster_name}-eks-encryption-key"
  target_key_id = aws_kms_key.eks.key_id
}