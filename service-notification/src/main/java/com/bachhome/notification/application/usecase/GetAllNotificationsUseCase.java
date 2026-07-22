package com.bachhome.notification.application.usecase;

import com.bachhome.notification.application.dto.NotificationDto;
import com.bachhome.notification.domain.model.Notification;
import com.bachhome.notification.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAllNotificationsUseCase {
    private final NotificationRepository repository;

    @Transactional(readOnly = true)
    public List<NotificationDto> execute() {
        return repository.findAllByOrderByCreatedAtDesc()
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
