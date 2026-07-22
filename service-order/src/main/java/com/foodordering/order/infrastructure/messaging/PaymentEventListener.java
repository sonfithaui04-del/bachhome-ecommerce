package com.foodordering.order.infrastructure.messaging;

import com.foodordering.order.domain.model.Order;
import com.foodordering.order.domain.repository.OrderRepository;
import com.foodordering.order.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Listener cho Payment Events từ Payment Service
 * Cập nhật paymentStatus trong Order khi có thanh toán thành công
 * Gửi notification đến user qua socket-service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderRepository orderRepository;
    private final AmqpTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_CONFIRMED_QUEUE)
    public void handlePaymentConfirmed(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            
            if (!"PAYMENT_CONFIRMED".equals(type)) {
                log.debug("[ORDER] Ignoring event type: {}", type);
                return;
            }

            Long orderId = Long.valueOf(event.get("orderId").toString());
            String paymentStatus = (String) event.get("paymentStatus");
            String transactionId = (String) event.get("transactionId");

            log.info("[ORDER] Received PAYMENT_CONFIRMED for Order {}, status={}", orderId, paymentStatus);

            // Cập nhật paymentStatus trong Order
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setPaymentStatus(paymentStatus);
                orderRepository.save(order);
                
                log.info("[ORDER] Updated paymentStatus to {} for Order {}", paymentStatus, orderId);
                
                // Gửi notification đến user qua socket-service
                sendPaymentNotification(order, transactionId);
            } else {
                log.warn("[ORDER] Order {} not found for payment update", orderId);
            }

        } catch (Exception e) {
            log.error("[ORDER] Error processing payment confirmed event: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi notification đến user thông báo thanh toán thành công
     */
    private void sendPaymentNotification(Order order, String transactionId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "PAYMENT_SUCCESS");
            notification.put("orderId", order.getId());
            notification.put("userId", order.getUserId());
            notification.put("message", "Đơn hàng #" + order.getId() + " đã được thanh toán thành công!");
            notification.put("transactionId", transactionId);
            notification.put("totalAmount", order.getTotalAmount());
            notification.put("timestamp", System.currentTimeMillis());

            // Gửi đến socket-service qua exchange để push notification đến frontend
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "order.status.changed", notification);
            log.info("[ORDER] Sent payment success notification for Order {}", order.getId());
        } catch (Exception e) {
            log.error("[ORDER] Failed to send payment notification: {}", e.getMessage());
        }
    }
}

