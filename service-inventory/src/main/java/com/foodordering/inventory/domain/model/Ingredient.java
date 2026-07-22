package com.foodordering.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ingredient Domain Model - Nguyên liệu
 */
@Entity
@Table(name = "ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String unit; // kg, lít, gói, v.v.

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity; // Số lượng tồn kho hiện tại

    @Column(precision = 10, scale = 2)
    private BigDecimal minQuantity; // Ngưỡng cảnh báo sắp hết

    @Column(precision = 10, scale = 2)
    private BigDecimal costPerUnit; // Giá nhập/đơn vị

    private LocalDate expiryDate; // Ngày hết hạn (optional)

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (quantity == null) {
            quantity = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra xem nguyên liệu có sắp hết không
     */
    public boolean isLowStock() {
        if (minQuantity == null) {
            return false;
        }
        return quantity.compareTo(minQuantity) <= 0;
    }

    /**
     * Thêm số lượng (nhập kho)
     */
    public void addQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        this.quantity = this.quantity.add(amount);
    }

    /**
     * Trừ số lượng (xuất kho)
     */
    public void deductQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (this.quantity.compareTo(amount) < 0) {
            throw new IllegalStateException("Không đủ số lượng trong kho");
        }
        this.quantity = this.quantity.subtract(amount);
    }
}
