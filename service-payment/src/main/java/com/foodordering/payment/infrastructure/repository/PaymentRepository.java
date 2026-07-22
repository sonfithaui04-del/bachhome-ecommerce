package com.foodordering.payment.infrastructure.repository;

import com.foodordering.payment.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    
    // Sử dụng findFirst để tránh lỗi khi có nhiều record cho cùng orderId
    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
}

