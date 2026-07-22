# Order Service - Implementation Summary

## ✅ Files Created (So far)

**Setup (4 files):**
- pom.xml
- OrderServiceApplication.java
- application.yml  
- SwaggerConfig.java

**Domain (3 files):**
- Order.java
- OrderItem.java
- OrderStatus.java
- OrderRepository.java (interface)

**Infrastructure (2 files):**
- JpaOrderRepository.java
- OrderRepositoryImpl.java

## 📋 Missing Files (To create)

### DTOs (4 files needed):
```java
// OrderDto.java
package com.foodordering.order.application.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String deliveryAddress;
    private String phoneNumber;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
}

// OrderItemDto.java  
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemDto {
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}

// CreateOrderDto.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateOrderDto {
    @NotNull private Long userId;
    @NotEmpty private List<OrderItemRequest> items;
    private String deliveryAddress;
    private String phoneNumber;
    private String notes;
}

// OrderItemRequest.java
@Data @Builder @NoArgsConstructor @AllArgsConstructor  
public class OrderItemRequest {
    @NotNull private Long menuItemId;
    @NotNull @Min(1) private Integer quantity;
}
```

### Use Cases (2 files):
```java
// CreateOrderUseCase.java
@Service @RequiredArgsConstructor @Slf4j
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
    
    @Transactional
    public OrderDto execute(CreateOrderDto request) {
        Order order = Order.builder()
            .userId(request.getUserId())
            .deliveryAddress(request.getDeliveryAddress())
            .phoneNumber(request.getPhoneNumber())
            .notes(request.getNotes())
            .build();
            
        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = OrderItem.builder()
                .menuItemId(itemReq.getMenuItemId())
                .menuItemName("Item " + itemReq.getMenuItemId()) // TODO: fetch from menu service
                .quantity(itemReq.getQuantity())
                .price(BigDecimal.valueOf(50000)) // TODO: fetch real price
                .build();
            order.addItem(item);
        }
        
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }
}

// GetUserOrdersUseCase.java
@Service @RequiredArgsConstructor @Slf4j
public class GetUserOrdersUseCase {
    private final OrderRepository orderRepository;
    
    public List<OrderDto> execute(Long userId) {
        return orderRepository.findByUserId(userId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
```

### Controller (1 file):
```java
// OrderController.java
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final GetUserOrdersUseCase getUserOrdersUseCase;
    
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderDto request) {
        OrderDto order = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        List<OrderDto> orders = getUserOrdersUseCase.execute(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running");
    }
}
```

### Dockerfile:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 🚀 Next: Create these files manually or let me continue?
