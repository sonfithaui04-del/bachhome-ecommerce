---
description: Bài 2 - Thêm Microservice Thứ Hai (Product Service)
---

# Bài 2: Thêm Microservice Thứ Hai (Product Service)

Mục tiêu: Tạo Product Service và để User Service gọi API của nó qua `RestTemplate`.

## 1. Tạo Project (Product Service)
- **Artifact:** `product-service`
- **Dependencies:** Spring Web

## 2. Viết Mã Nguồn Product Service

### 2.1. Model: `Product.java`
`src/main/java/com/example/productservice/model/Product.java`

```java
package com.example.productservice.model;

public class Product {
    private Long id;
    private String name;

    public Product(Long id, String name) {
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

### 2.2. Controller: `ProductController.java`
`src/main/java/com/example/productservice/controller/ProductController.java`

```java
package com.example.productservice.controller;

import com.example.productservice.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
public class ProductController {
    @GetMapping("/products")
    public List<Product> getProducts() {
        return Arrays.asList(
            new Product(1L, "Laptop"),
            new Product(2L, "Phone")
        );
    }
}
```

### 2.3. Config: `application.properties`
```properties
server.port=8082
spring.application.name=product-service
```

## 3. Cập Nhật User Service (Gọi Product)

### 3.1. Service Client: `ProductClient.java`
`src/main/java/com/example/userservice/service/ProductClient.java`

```java
package com.example.userservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public String getProducts() {
        // Hardcoded URL for Lab 2
        return restTemplate.getForObject("http://localhost:8082/products", String.class);
    }
}
```

### 3.2. Update Controller: `UserController.java`
Thêm endpoint mới vào `UserController`:

```java
    private final ProductClient productClient;

    public UserController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping("/user-products")
    public String getUserProducts() {
        return "Users and Products: " + productClient.getProducts();
    }
```

## 4. Dockerize & K8s

### 4.1. Dockerfile (Product Service)
Giống Bài 1.

### 4.2. Deployment YAML (Product Service)
Tạo `deployment-product.yml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
      - name: product-service
        image: product-service:1.0
        ports:
        - containerPort: 8082
---
apiVersion: v1
kind: Service
metadata:
  name: product-service
spec:
  selector:
    app: product-service
  ports:
    - port: 80
      targetPort: 8082
  type: ClusterIP
```

### 4.3. Update User Service for K8s
Để chạy trong K8s, URL trong `ProductClient.java` cần đổi thành tên Service K8s:
`http://product-service/products` (thay vì localhost:8082).

Sau đó rebuild image `user-service:1.0` và apply lại.
