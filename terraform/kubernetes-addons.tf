# Kubernetes Add-ons and Controllers

# AWS Load Balancer Controller
resource "helm_release" "aws_load_balancer_controller" {
  count      = var.enable_aws_load_balancer_controller ? 1 : 0
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"
  version    = "1.6.2"

  set {
    name  = "clusterName"
    value = module.eks.cluster_name
  }

  set {
    name  = "serviceAccount.create"
    value = "true"
  }

  set {
    name  = "serviceAccount.name"
    value = "aws-load-balancer-controller"
  }

  set {
    name  = "serviceAccount.annotations.eks\.amazonaws\.com/role-arn"
    value = aws_iam_role.aws_load_balancer_controller[0].arn
  }

  set {
    name  = "region"
    value = var.aws_region
  }

  set {
    name  = "vpcId"
    value = module.vpc.vpc_id
  }

  depends_on = [
    module.eks,
    aws_iam_role_policy_attachment.aws_load_balancer_controller
  ]
}

# Cluster Autoscaler
resource "helm_release" "cluster_autoscaler" {
  count      = var.enable_cluster_autoscaler ? 1 : 0
  name       = "cluster-autoscaler"
  repository = "https://kubernetes.github.io/autoscaler"
  chart      = "cluster-autoscaler"
  namespace  = "kube-system"
  version    = "9.29.0"

  set {
    name  = "autoDiscovery.clusterName"
    value = module.eks.cluster_name
  }

  set {
    name  = "awsRegion"
    value = var.aws_region
  }

  set {
    name  = "serviceAccount.create"
    value = "true"
  }

  set {
    name  = "serviceAccount.name"
    value = "cluster-autoscaler"
  }

  set {
    name  = "serviceAccount.annotations.eks\.amazonaws\.com/role-arn"
    value = aws_iam_role.cluster_autoscaler[0].arn
  }

  set {
    name  = "rbac.create"
    value = "true"
  }

  set {
    name  = "rbac.serviceAccount.create"
    value = "true"
  }

  set {
    name  = "extraArgs.scale-down-delay-after-add"
    value = "10m"
  }

  set {
    name  = "extraArgs.scale-down-unneeded-time"
    value = "10m"
  }

  depends_on = [
    module.eks,
    aws_iam_role_policy_attachment.cluster_autoscaler
  ]
}

# Metrics Server (required for HPA)
resource "helm_release" "metrics_server" {
  count      = var.enable_metrics_server ? 1 : 0
  name       = "metrics-server"
  repository = "https://kubernetes-sigs.github.io/metrics-server/"
  chart      = "metrics-server"
  namespace  = "kube-system"
  version    = "3.11.0"

  set {
    name  = "args"
    value = "{--cert-dir=/tmp,--secure-port=4443,--kubelet-preferred-address-types=InternalIP\,ExternalIP\,Hostname,--kubelet-use-node-status-port,--metric-resolution=15s}"
  }

  depends_on = [module.eks]
}

# AWS for Fluent Bit (for CloudWatch logging)
resource "helm_release" "aws_for_fluent_bit" {
  count      = var.enable_cloudwatch_logging ? 1 : 0
  name       = "aws-for-fluent-bit"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-for-fluent-bit"
  namespace  = "amazon-cloudwatch"
  version    = "0.1.32"
  create_namespace = true

  set {
    name  = "cloudWatchLogs.enabled"
    value = "true"
  }

  set {
    name  = "cloudWatchLogs.region"
    value = var.aws_region
  }

  set {
    name  = "cloudWatchLogs.logGroupName"
    value = "/aws/eks/customer-service"
  }

  set {
    name  = "cloudWatchLogs.logStreamPrefix"
    value = "fluent-bit-"
  }

  set {
    name  = "firehose.enabled"
    value = "false"
  }

  set {
    name  = "kinesis.enabled"
    value = "false"
  }

  set {
    name  = "elasticsearch.enabled"
    value = "false"
  }

  depends_on = [
    module.eks,
    aws_cloudwatch_log_group.customer_service
  ]
}

# Customer Service Namespace
resource "kubernetes_namespace" "customer_service" {
  metadata {
    name = "customer-service"
    
    labels = {
      name = "customer-service"
      "istio-injection" = "disabled"
    }
  }

  depends_on = [module.eks]
}

# Customer Service ConfigMap
resource "kubernetes_config_map" "customer_service_config" {
  metadata {
    name      = "customer-service-config"
    namespace = kubernetes_namespace.customer_service.metadata[0].name
  }

  data = {
    "SPRING_PROFILES_ACTIVE" = "production"
    "SERVER_PORT"           = "8080"
    "LOGGING_LEVEL_ROOT"    = "WARN"
    "LOGGING_LEVEL_COM_EXAMPLE_CUSTOMER" = "INFO"
    "MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE" = "health,info,metrics"
    "MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS" = "always"
  }

  depends_on = [kubernetes_namespace.customer_service]
}

# Customer Service Secret (for sensitive configuration)
resource "kubernetes_secret" "customer_service_secret" {
  metadata {
    name      = "customer-service-secret"
    namespace = kubernetes_namespace.customer_service.metadata[0].name
  }

  type = "Opaque"

  data = {
    # Add any sensitive configuration here
    # Example: database passwords, API keys, etc.
  }

  depends_on = [kubernetes_namespace.customer_service]
}