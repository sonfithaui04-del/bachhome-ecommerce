# Academic & Lab Exercise Rules

This document defines the coding standards and workflow for handling "Practice Labs" (Bài thực hành), University Exercises, or POCs. These rules take precedence over production standards when the user request is identified as a generic exercise or specific lab instruction.

## 1. Philosophy: "Follow the Lab Manual"
*   **Simplicity over Complexity:** Do not use Clean Architecture (Hexagonal) unless explicitly asked. Use standard **MVC** (Controller -> Service -> Repository -> Model).
*   **Direct Implementation:** If the lab says "return User entity", return the Entity. Do NOT create DTOs unless the lab requires it.
*   **Hardcoding is Allowed:** If the lab says "hardcode URL http://localhost:8082", do it. Do not create complex config files unless asked.

## 2. Technology Stack (Lab Standard)
*   **Java:** JDK 17 or 21.
*   **Spring Boot:** Latest stable (or specific version requested, e.g., 3.5.6).
*   **Build Tool:** Maven.
*   **Containerization:** Docker (Dockerfile per service).
*   **Orchestration:** Kubernetes (Minikube).

## 3. Microservice Patterns (Academic Style)

### A. Service Structure
*   **Controller:** Contains endpoints. Can return `List<Entity>` or `String`.
*   **Model:** Simple POJOs with Getters/Setters/Constructors.
*   **Service:** Optional. Logic can be in Controller for very simple labs.

### B. Communication
*   **Synchronous:** Use `RestTemplate` (preferred for labs) or `WebClient`.
*   **Discovery:** Netflix Eureka (`@EnableEurekaServer`, `@EnableDiscoveryClient`).
*   **Gateway:** Spring Cloud Gateway (`routes` in `application.yml`).

### C. Security
*   **Basic Auth:** Often implemented via Gateway Filters checking Headers manually (e.g., `Authorization: Basic ...`).
*   **Config:** Simple `SecurityConfig` with `httpBasic()` and `permitAll()` or `authenticated()`.

## 4. Deployment Workflow (Docker & K8s)

### Docker
*   **Base Image:** `openjdk:17-jdk-slim` (or similar).
*   **Build:** `mvn clean package` -> `docker build`.
*   **Run:** `docker run -p <host>:<container> <image>`.

### Kubernetes (Minikube)
*   **Manifests:** Create `deployment.yml` (Deployment + Service).
*   **Service Type:** `LoadBalancer` (for external access) or `ClusterIP` (internal).
*   **ConfigMap:** Use for environment variables (DB credentials, Auth users).
*   **Scaling:** `kubectl scale deployment ...`

## 5. Example Project Structure (User Service)
```
src/main/java/com/example/userservice/
├── controller/
│   └── UserController.java
├── model/
│   └── User.java
└── UserServiceApplication.java
```

## 6. Response Strategy for Lab Requests
1.  **Analyze Requirements:** Identify the specific steps (Step 1, Step 2...).
2.  **Generate Code:** Write the exact classes requested (Model, Controller).
3.  **Config:** Write `application.properties` or `yml`.
4.  **Deployment:** Immediately provide `Dockerfile` and `deployment.yml`.
5.  **Commands:** Provide the exact `docker build`, `kubectl apply`, and `minikube service` commands.
