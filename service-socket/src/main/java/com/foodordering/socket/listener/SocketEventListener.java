package com.foodordering.socket.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @RabbitListener(queues = "socket-notification-queue")
    public void handleEvent(Object event) {
        log.info("🔥 [SOCKET-SERVICE] Received event type: {}", event.getClass().getName());
        
        Map<String, Object> map = null;

        try {
            if (event instanceof Map) {
                map = (Map<String, Object>) event;
            } else if (event instanceof org.springframework.amqp.core.Message) {
                org.springframework.amqp.core.Message msg = (org.springframework.amqp.core.Message) event;
                map = objectMapper.readValue(msg.getBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } else if (event instanceof byte[]) {
                map = objectMapper.readValue((byte[]) event, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            } else {
                log.warn("Unknown event type, trying toString parsing or ignoring");
            }
        } catch (Exception e) {
            log.error("Error parsing event data", e);
            return;
        }

        if (map != null) {
            String type = (String) map.getOrDefault("type", "UNKNOWN");
            
            // Construct notification payload
            Map<String, Object> notification = new java.util.HashMap<>(map);
            String message = "";
            String subject = "";
            
            if (map.containsKey("orderId")) {
                Long orderId = ((Number) map.get("orderId")).longValue();
                
                // Handle CHAT_MESSAGE - Relay chat to all participants of that order
                if ("CHAT_MESSAGE".equals(type)) {
                    String destination = "/topic/orders/" + orderId + "/chat";
                    messagingTemplate.convertAndSend(destination, map);
                    log.info("   -> Relayed CHAT_MESSAGE to {}", destination);
                    return;
                }

                // Lấy customerName, nếu không có thì dùng email hoặc "Khách hàng"
                String customerName = (String) map.get("customerName");
                if (customerName == null || customerName.isEmpty()) {
                    customerName = (String) map.get("email");
                    if (customerName == null || customerName.isEmpty()) {
                        customerName = "Khách hàng";
                    }
                }
                
                // Handle PAYMENT_SUCCESS - Gửi thông báo thanh toán thành công cho User
                if ("PAYMENT_SUCCESS".equals(type)) {
                    if (map.containsKey("userId")) {
                        String userId = String.valueOf(map.get("userId"));
                        Map<String, Object> userNotification = new java.util.HashMap<>(notification);
                        userNotification.put("type", "PAYMENT_SUCCESS");
                        userNotification.put("subject", "Thanh toán thành công 🎉");
                        userNotification.put("message", map.getOrDefault("message", "Đơn hàng #" + orderId + " đã được thanh toán thành công!"));
                        userNotification.put("createdAt", new java.util.Date());
                        
                        messagingTemplate.convertAndSend("/topic/user/" + userId, userNotification);
                        log.info("   -> Pushed PAYMENT_SUCCESS to user {}", userId);
                    }
                    return; // Đã xử lý xong, không cần xử lý tiếp
                }
                
                if ("ORDER_STATUS_CHANGED".equals(type)) {
                    String status = (String) map.get("status");
                    subject = "Đơn hàng #" + orderId + " đã cập nhật";
                    message = "Trạng thái đơn hàng đã chuyển sang " + getStatusDisplay(status);
                    
                    // Gửi thông báo cho User khi status thay đổi (từ CONFIRMED trở đi)
                    if (map.containsKey("userId") && shouldNotifyUser(status)) {
                        String userId = String.valueOf(map.get("userId"));
                        Map<String, Object> userNotification = new java.util.HashMap<>(notification);
                        userNotification.put("subject", "Đơn hàng của bạn đã được cập nhật");
                        userNotification.put("message", message);
                        userNotification.put("createdAt", new java.util.Date());
                        
                        messagingTemplate.convertAndSend("/topic/user/" + userId, userNotification);
                        log.info("   -> Pushed status update to user {}", userId);
                    }
                    
                    // Gửi cho Admin
                    notification.put("subject", subject);
                    notification.put("message", message);
                    notification.put("createdAt", new java.util.Date());
                    messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
                    log.info("   -> Broadcasted status update to admin");
                    
                } else {
                    // Đơn hàng mới - CHỈ gửi cho Admin
                    subject = "Đơn hàng mới #" + orderId;
                    message = customerName + " vừa đặt đơn hàng mới";
                    
                    notification.put("subject", subject);
                    notification.put("message", message);
                    notification.put("createdAt", new java.util.Date());
                    
                    // Chỉ gửi cho Admin, KHÔNG gửi cho User
                    messagingTemplate.convertAndSend("/topic/admin/notifications", notification);
                    log.info("   -> Broadcasted new order to admin only (customerName: {})", customerName);
                }
            }
        }
    }
    
    /**
     * Kiểm tra xem có nên thông báo cho User không
     * Chỉ thông báo khi status từ CONFIRMED trở đi
     */
    private boolean shouldNotifyUser(String status) {
        return status != null && (
            "CONFIRMED".equals(status) ||
            "PREPARING".equals(status) ||
            "READY".equals(status) ||
            "DELIVERING".equals(status) ||
            "DELIVERED".equals(status) ||
            "COMPLETED".equals(status) ||
            "CANCELLED".equals(status)
        );
    }
    
    /**
     * Hiển thị status bằng tiếng Việt
     */
    private String getStatusDisplay(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "PREPARING": return "Đang chuẩn bị";
            case "READY": return "Sẵn sàng giao";
            case "DELIVERING": return "Đang giao hàng";
            case "DELIVERED": return "Đã giao";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}
