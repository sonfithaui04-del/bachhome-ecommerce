---
description: Bài 3 - Triển Khai API Gateway Cơ Bản
---

# Bài 3: Triển Khai API Gateway Cơ Bản

Mục tiêu: Tạo API Gateway để route request đến User Service và Product Service.

## 1. Tạo Project (API Gateway)
- **Artifact:** `api-gateway`
- **Dependencies:** Spring Cloud Gateway, Spring Boot Actuator

## 2. Cấu hình Gateway

### 2.1. `application.yml`
`src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-route
          uri: http://localhost:8081
          predicates:
            - Path=/users/**
        - id: product-route
          uri: http://localhost:8082
          predicates:
            - Path=/products/**
```

## 3. Dockerize & K8s

### 3.1. Dockerfile
Giống các bài trước.

### 3.2. Deployment YAML
Tạo `deployment-gateway.yml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 1
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: api-gateway:1.0
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  selector:
    app: api-gateway
  ports:
    - port: 80
      targetPort: 8080
  type: LoadBalancer
```

### 3.3. Update Config for K8s
Trong môi trường K8s, `application.yml` cần trỏ đến tên Service K8s:

```yaml
      routes:
        - id: user-route
          uri: http://user-service
          predicates:
            - Path=/users/**
        - id: product-route
          uri: http://product-service
          predicates:
            - Path=/products/**
```
