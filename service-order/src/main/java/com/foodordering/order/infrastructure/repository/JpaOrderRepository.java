package com.foodordering.order.infrastructure.repository;

import com.foodordering.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByStatus(com.foodordering.order.domain.model.OrderStatus status);
    List<Order> findByShipperIdOrderByCreatedAtDesc(Long shipperId);
}
