package com.foodordering.notification.infrastructure.repository;

import com.foodordering.notification.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    java.util.List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    java.util.List<Notification> findAllByOrderByCreatedAtDesc();
}
