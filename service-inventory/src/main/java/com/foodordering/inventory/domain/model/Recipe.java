package com.foodordering.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recipe Domain Model - Công thức món ăn
 * Định nghĩa MenuItem cần bao nhiêu Ingredient
 */
@Entity
@Table(name = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long menuItemId; // FK tới MenuItem (service-menu)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity; // Số lượng nguyên liệu cần cho 1 phần món ăn

    @Column(length = 200)
    private String notes; // Ghi chú (optional)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Tính tổng số lượng nguyên liệu cần cho N phần món ăn
     */
    public BigDecimal calculateRequiredQuantity(int portions) {
        if (portions <= 0) {
            throw new IllegalArgumentException("Số phần phải lớn hơn 0");
        }
        return quantity.multiply(BigDecimal.valueOf(portions));
    }
}
