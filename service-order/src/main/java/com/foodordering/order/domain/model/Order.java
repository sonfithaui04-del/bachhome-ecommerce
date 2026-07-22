package com.foodordering.order.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Domain Model - Aggregate Root
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(length = 500)
    private String deliveryAddress;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String customerName;

    @Column(length = 1000)
    private String notes;

    @Column(length = 20)
    private String paymentMethod;  // COD, SEPAY, MOMO, etc.

    @Column(name = "points_used")
    private Integer pointsUsed;

    @Column(length = 30)
    private String paymentStatus;  // PENDING, SUCCESS, FAILED

    @Column(name = "shipper_id")
    private Long shipperId;

    private LocalDateTime assignedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        if (pointsUsed != null && pointsUsed > 0) {
            BigDecimal discount = BigDecimal.valueOf(pointsUsed * 1000L);
            this.totalAmount = this.totalAmount.subtract(discount);
            if (this.totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                this.totalAmount = BigDecimal.ZERO;
            }
        }
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == OrderStatus.CONFIRMED || this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.CANCELLED;
        } else {
            throw new IllegalStateException("Cannot cancel order in status: " + this.status);
        }
    }

    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.CONFIRMED;
    }
}
