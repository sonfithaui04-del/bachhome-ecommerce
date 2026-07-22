---
description: Bài 5 - Microservice Với Authentication Qua API Gateway
---

# Bài 5: Microservice Với Authentication Qua API Gateway

Mục tiêu: Thêm bảo mật (Basic Auth) tại Gateway và User Service.

## 1. Cấu hình Security tại Gateway

### 1.1. AuthFilter
Tạo file `src/main/java/com/example/apigateway/filter/AuthFilter.java`:

```java
package com.example.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        // Check Basic Auth: user:pass (base64 encoded is dXNlcjpwYXNz)
        if (auth == null || !auth.equals("Basic dXNlcjpwYXNz")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
```

## 2. Bảo Vệ User Service

### 2.1. Dependencies
Thêm `Spring Security` vào `pom.xml` của `user-service`.

### 2.2. SecurityConfig
Tạo file `src/main/java/com/example/userservice/config/SecurityConfig.java`:

```java
package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

## 3. ConfigMap & Secrets (K8s)

### 3.1. ConfigMap YAML
Tạo file `configmap.yml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  AUTH_USER: user
  AUTH_PASS: pass
```

### 3.2. Update AuthFilter (Environment Variables)
Cập nhật `AuthFilter` để đọc từ biến môi trường thay vì hardcode:

```java
    // Trong class AuthFilter
    // @Value("${AUTH_USER}") private String user; ...
    // Logic check sẽ so sánh với biến môi trường này
```

### 3.3. Update Deployment (Gateway)
Thêm `envFrom` vào `deployment-gateway.yml`:

```yaml
    spec:
      containers:
      - name: api-gateway
        image: api-gateway:1.0
        envFrom:
        - configMapRef:
            name: app-config
```

## 4. Scaling (K8s)
Tăng số lượng pod của User Service lên 2:

```bash
kubectl scale deployment user-service --replicas=2
```
