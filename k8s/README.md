# 🚀 Kubernetes Deployment Guide

## Tổng quan

Thư mục này chứa các file manifest Kubernetes để triển khai hệ thống Food Ordering lên K8s cluster.

## 📁 Cấu trúc thư mục

```
k8s/
├── namespace.yaml      # Namespace cho ứng dụng
├── configmap.yaml      # ConfigMap và Secrets
├── deployments.yaml    # Deployments cho các microservices
├── databases.yaml      # StatefulSets cho databases + RabbitMQ
└── ingress.yaml        # Ingress và HPA (Auto-scaling)
```

## 🛠️ Yêu cầu

- Kubernetes cluster (minikube, kind, hoặc cloud provider)
- kubectl CLI đã cài đặt
- Docker images đã được build và push lên registry

## 📦 Triển khai

### 1. Tạo Namespace
```bash
kubectl apply -f k8s/namespace.yaml
```

### 2. Tạo ConfigMap và Secrets
```bash
kubectl apply -f k8s/configmap.yaml
```

### 3. Triển khai Databases và RabbitMQ
```bash
kubectl apply -f k8s/databases.yaml
```

### 4. Triển khai Microservices
```bash
kubectl apply -f k8s/deployments.yaml
```

### 5. Triển khai Ingress và HPA
```bash
kubectl apply -f k8s/ingress.yaml
```

### Hoặc triển khai tất cả cùng lúc:
```bash
kubectl apply -f k8s/
```

## 🔍 Kiểm tra trạng thái

```bash
# Xem tất cả pods
kubectl get pods -n food-ordering

# Xem services
kubectl get svc -n food-ordering

# Xem deployments
kubectl get deployments -n food-ordering

# Xem logs của một pod
kubectl logs -f <pod-name> -n food-ordering
```

## 🌐 Truy cập ứng dụng

### Với Minikube:
```bash
minikube service api-gateway -n food-ordering
```

### Với Ingress:
Thêm vào file hosts:
```
127.0.0.1 food-ordering.local
```

Truy cập: http://food-ordering.local

## 📊 Monitoring

```bash
# Xem HPA status
kubectl get hpa -n food-ordering

# Xem resource usage
kubectl top pods -n food-ordering
```

## 🗑️ Cleanup

```bash
kubectl delete namespace food-ordering
```

---

**Ngày tạo:** 2025-12-06
