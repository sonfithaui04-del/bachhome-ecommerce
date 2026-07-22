package com.foodordering.notification.interfaces.controller;

import com.foodordering.notification.application.dto.CreateNotificationDto;
import com.foodordering.notification.application.dto.NotificationDto;
import com.foodordering.notification.application.usecase.SendNotificationUseCase;
import com.foodordering.notification.application.usecase.GetNotificationsUseCase;
import com.foodordering.notification.application.usecase.GetAllNotificationsUseCase;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SendNotificationUseCase sendNotificationUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final GetAllNotificationsUseCase getAllNotificationsUseCase;
    
    @PostMapping
    public ResponseEntity<NotificationDto> send(@RequestBody CreateNotificationDto request) {
        NotificationDto notification = sendNotificationUseCase.execute(request);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<java.util.List<NotificationDto>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(getNotificationsUseCase.execute(userId));
    }

    @GetMapping
    public ResponseEntity<java.util.List<NotificationDto>> getAllNotifications() {
        return ResponseEntity.ok(getAllNotificationsUseCase.execute());
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
