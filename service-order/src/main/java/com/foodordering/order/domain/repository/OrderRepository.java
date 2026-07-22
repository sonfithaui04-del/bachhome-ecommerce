package com.foodordering.order.domain.repository;

import com.foodordering.order.domain.model.Order;
import com.foodordering.order.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    List<Order> findAll();
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByShipperId(Long shipperId);
    Order save(Order order);
    void deleteById(Long id);
}
