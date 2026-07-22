---
description: Bài 1 - Xây Dựng Microservice Cơ Bản (User Service)
---

# Bài 1: Xây Dựng Microservice Cơ Bản (User Service)

Mục tiêu: Xây dựng một Microservice quản lý user đơn giản, trả về danh sách user qua REST API, đóng gói Docker và triển khai lên Minikube.

## 1. Tạo Project (User Service)
Tạo project Spring Boot với cấu trúc sau:
- **Group:** `com.example`
- **Artifact:** `user-service`
- **Dependencies:** Spring Web, Spring Boot Actuator

## 2. Viết Mã Nguồn

### 2.1. Model: `User.java`
Tạo file: `src/main/java/com/example/userservice/model/User.java`

```java
package com.example.userservice.model;

public class User {
    private Long id;
    private String name;

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

### 2.2. Controller: `UserController.java`
Tạo file: `src/main/java/com/example/userservice/controller/UserController.java`

```java
package com.example.userservice.controller;

import com.example.userservice.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
public class UserController {
    @GetMapping("/users")
    public List<User> getUsers() {
        return Arrays.asList(
            new User(1L, "Alice"),
            new User(2L, "Bob")
        );
    }
}
```

### 2.3. Cấu hình: `application.properties`
Sửa file: `src/main/resources/application.properties`

```properties
server.port=8081
spring.application.name=user-service
```

## 3. Dockerize

### 3.1. Dockerfile
Tạo file `Dockerfile` tại thư mục gốc của project:

```dockerfile
FROM openjdk:17-jdk-slim
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 3.2. Build & Run Docker
```bash
# 1. Build JAR
mvn clean package

# 2. Build Image
docker build -t user-service:1.0 .

# 3. Run Container
docker run -p 8081:8081 user-service:1.0
```

## 4. Triển Khai Kubernetes (Minikube)

### 4.1. Deployment YAML
Tạo file `deployment.yml` tại thư mục gốc:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:1.0
        ports:
        - containerPort: 8081
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service
  ports:
    - port: 80
      targetPort: 8081
  type: LoadBalancer
```

### 4.2. Deploy Commands
```bash
# 1. Start Minikube
minikube start

# 2. Build image inside Minikube (Quan trọng)
eval $(minikube docker-env)
docker build -t user-service:1.0 .

# 3. Apply Deployment
kubectl apply -f deployment.yml

# 4. Access Service
minikube service user-service
```
