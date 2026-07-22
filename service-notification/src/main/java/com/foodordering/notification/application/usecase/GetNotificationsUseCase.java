package com.foodordering.notification.application.usecase;

import com.foodordering.notification.application.dto.NotificationDto;
import com.foodordering.notification.domain.model.Notification;
import com.foodordering.notification.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetNotificationsUseCase {
    private final NotificationRepository repository;

    @Transactional(readOnly = true)
    public List<NotificationDto> execute(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
