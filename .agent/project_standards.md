# Project Standards & Coding Rules

This document outlines the architectural standards, coding conventions, and best practices established for the Food Ordering Microservices project.

## 1. Technology Stack

*   **Backend:** Java 17+, Spring Boot 3.x
*   **Frontend:** React 18 (Vite), Tailwind CSS
*   **Database:** PostgreSQL
*   **Message Broker:** RabbitMQ
*   **Service Discovery:** Netflix Eureka
*   **API Gateway:** Spring Cloud Gateway
*   **Containerization:** Docker & Docker Compose

## 2. Architecture Overview

The project follows a **Microservices Architecture** with **Event-Driven** communication for asynchronous tasks and **REST APIs** for synchronous operations.

### Backend Structure (Clean Architecture / Hexagonal Style)
Each microservice follows a strict layered architecture:

1.  **Interfaces Layer (`interfaces`)**:
    *   Contains REST Controllers (`*Controller.java`).
    *   Handles HTTP requests/responses.
    *   **Rule:** Controllers should only call Use Cases, not Repositories directly.
2.  **Application Layer (`application`)**:
    *   **Use Cases (`usecase`)**: Contains business logic classes.
    *   **DTOs (`dto`)**: Data Transfer Objects for input/output.
    *   **Rule:** Always use DTOs for API responses. Never expose Entity classes directly to the client.
3.  **Domain Layer (`domain`)**:
    *   **Models (`model`)**: JPA Entities / Aggregates.
    *   **Repositories (`repository`)**: Interfaces defining data access contracts.
    *   **Rule:** This layer should be independent of frameworks as much as possible.
4.  **Infrastructure Layer (`infrastructure`)**:
    *   **Repository Impl (`repository`)**: Implementation of Domain Repository interfaces (using Spring Data JPA).
    *   **Config (`config`)**: Configuration classes (Security, RabbitMQ, Swagger).
    *   **Messaging (`messaging`)**: RabbitMQ Listeners/Publishers.

### Frontend Structure
1.  **Components**: Reusable UI elements.
2.  **Pages**: Route-specific views.
3.  **Services**: API calls (Axios) and WebSocket logic.
4.  **Context**: Global state management (Auth, Cart).

## 3. Communication Patterns

### Inter-service Communication
*   **Synchronous:** REST calls via `FeignClient` or `RestTemplate` (limited use).
*   **Asynchronous (Preferred):** RabbitMQ Events.
    *   **Format:** JSON payload.
    *   **Naming:** `service-name.event-type` (e.g., `order.status.changed`).

### Real-time Updates
*   **Protocol:** WebSocket (STOMP over SockJS).
*   **Gateway:** All WebSocket traffic goes through API Gateway port 8080 (`/ws`).
*   **Pattern:**
    *   **Singleton Service:** The Socket Service in frontend MUST be a Singleton to prevent duplicate connections/subscriptions (especially in React Strict Mode).
    *   **Topics:**
        *   Public/Admin: `/topic/admin/notifications`
        *   User-specific: `/topic/user/{userId}`

## 4. Coding Rules & Best Practices

### Backend
1.  **DTO Mapping:** Explicitly map Entities to DTOs (using Builders or Mappers) before returning from Controllers.
2.  **Enum Handling:** Ensure Java Enums match Frontend constants exactly.
    *   *Example:* `OrderStatus.COMPLETED` must map to `COMPLETED` in frontend, not `DELIVERED`.
3.  **Security:**
    *   Endpoints requiring access must be explicitly configured in `SecurityConfig`.
    *   Public endpoints must be added to `permitAll()`.
4.  **Error Handling:** Use Global Exception Handler (`@ControllerAdvice`) to return standard JSON error responses.

### Frontend
1.  **Socket Connection:**
    *   Use the `connectSocket` singleton pattern.
    *   Do NOT call `disconnect()` in `useEffect` cleanup if using the singleton pattern (to avoid reconnection loops in Strict Mode).
    *   Call `disconnectSocket()` explicitly on **Logout**.
2.  **State Management:** Use `Context API` for global state (User, Cart) and local state for UI components.
3.  **Styling:** Use Tailwind CSS utility classes.

## 5. Docker & Deployment
*   **Service Names:** Must match `spring.application.name` in `application.yml`.
*   **Networking:** All services communicate within the `food-ordering-network`.
*   **Gateway:** Exposes port `8080`. Frontend apps connect to `localhost:8080`.

## 6. Critical Fixes History (Reference)
*   **Duplicate Toasts:** Caused by multiple socket connections. Fixed by implementing Singleton pattern in `socketService.js`.
*   **404 on New Endpoints:** Caused by `SecurityConfig` blocking new paths. Fixed by adding paths (e.g., `/users/**`) to `permitAll` or authenticated lists.
*   **Enum Mismatch:** Backend `COMPLETED` vs Frontend `DELIVERED`. Fixed by standardizing on `COMPLETED`.
