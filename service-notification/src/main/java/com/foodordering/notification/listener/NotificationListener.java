package com.foodordering.notification.listener;

import com.foodordering.notification.application.dto.CreateNotificationDto;
import com.foodordering.notification.application.usecase.SendNotificationUseCase;
import com.foodordering.notification.config.RabbitMQConfig;
import com.foodordering.notification.dto.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final SendNotificationUseCase sendNotificationUseCase;

    @RabbitListener(queues = RabbitMQConfig.ORDER_NOTIFICATION_QUEUE)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[NOTIFICATION-SERVICE] Received Order Confirmed Event for Order ID: {}", event.getOrderId());
        
        CreateNotificationDto notification = CreateNotificationDto.builder()
            .userId(event.getUserId() != null ? event.getUserId() : 1L) // Fallback if null
            .email(event.getEmail())
            .type("EMAIL")
            .subject("Order Confirmed: #" + event.getOrderId())
            .message("Your order #" + event.getOrderId() + " has been confirmed and is being prepared.")
            .build();

        sendNotificationUseCase.execute(notification);
    }
}
