---
description: Bài 4 - Thêm Service Discovery Với Eureka
---

# Bài 4: Thêm Service Discovery Với Eureka

Mục tiêu: Sử dụng Netflix Eureka để các service tự động tìm kiếm nhau thay vì hardcode URL.

## 1. Tạo Project (Eureka Server)
- **Artifact:** `eureka-server`
- **Dependencies:** Spring Cloud Netflix Eureka Server

## 2. Cấu hình Eureka Server

### 2.1. Main Class
Thêm annotation `@EnableEurekaServer`:

```java
package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

### 2.2. `application.yml`
```yaml
server:
  port: 8761

eureka:
  client:
    registerWithEureka: false
    fetchRegistry: false
  server:
    enableSelfPreservation: false # For dev
```

## 3. Cập Nhật Client Services (User, Product, Gateway)

### 3.1. Dependencies
Thêm vào `pom.xml` của cả 3 services: `Spring Cloud Netflix Eureka Client`.

### 3.2. Config (`application.yml`)
Thêm cấu hình Eureka Client vào `application.yml` của User, Product và Gateway:

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### 3.3. Cập Nhật Gateway Routes
Sửa `uri` trong Gateway để dùng Load Balancer (`lb://`):

```yaml
      routes:
        - id: user-route
          uri: lb://user-service
          predicates:
            - Path=/users/**
        - id: product-route
          uri: lb://product-service
          predicates:
            - Path=/products/**
```

## 4. Dockerize & K8s

### 4.1. Dockerfile (Eureka Server)
Giống các bài trước.

### 4.2. Deployment YAML (Eureka Server)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: eureka-server:1.0
        ports:
        - containerPort: 8761
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
spec:
  selector:
    app: eureka-server
  ports:
    - port: 8761
      targetPort: 8761
  type: ClusterIP
```

### 4.3. Update Config for K8s
Trong môi trường K8s, `defaultZone` cần trỏ đến Service K8s:
`http://eureka-server:8761/eureka/`
