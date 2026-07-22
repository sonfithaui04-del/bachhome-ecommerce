package com.foodordering.notification.application.usecase;

import com.foodordering.notification.application.dto.CreateNotificationDto;
import com.foodordering.notification.application.dto.NotificationDto;
import com.foodordering.notification.domain.model.Notification;
import com.foodordering.notification.infrastructure.repository.NotificationRepository;
import com.foodordering.notification.infrastructure.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotificationUseCase {
    private final NotificationRepository repository;
    private final EmailService emailService;
    
    @Transactional
    public NotificationDto execute(CreateNotificationDto request) {
        Notification notification = Notification.builder()
            .userId(request.getUserId())
            .type(request.getType())
            .subject(request.getSubject())
            .message(request.getMessage())
            .sent(true)
            .build();
        
        log.info("📧 PROCESSING NOTIFICATION: {} to user {}", notification.getSubject(), notification.getUserId());
        
        // Send Email if email is provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            emailService.sendEmail(request.getEmail(), request.getSubject(), request.getMessage());
        } else {
            log.warn("No email provided for notification to user {}", request.getUserId());
        }
        
        Notification saved = repository.save(notification);
        return toDto(saved);
    }

    private NotificationDto toDto(Notification notification) {
        return NotificationDto.builder()
            .id(notification.getId())
            .userId(notification.getUserId())
            .type(notification.getType())
            .subject(notification.getSubject())
            .message(notification.getMessage())
            .sent(notification.getSent())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
