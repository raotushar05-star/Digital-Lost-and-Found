package com.lostandfound.service;

import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.dto.notification.NotificationDto;
import com.lostandfound.entity.Case;
import com.lostandfound.entity.Match;
import com.lostandfound.entity.Notification;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.NotificationMapper;
import com.lostandfound.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public Notification notify(User user, NotificationType type, String title, String message, Case relatedCase, Match relatedMatch) {
        Notification notification = Notification.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .relatedCase(relatedCase)
                .relatedMatch(relatedMatch)
                .build();
        return notificationRepository.save(notification);
    }

    public PagedResponse<NotificationDto> getMyNotifications(UUID userId, int page, int size) {
        Page<Notification> result = notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PagedResponse.of(result.map(notificationMapper::toDto));
    }

    @Transactional
    public NotificationDto markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!notification.getUser().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found: " + notificationId);
        }
        notification.setIsRead(true);
        return notificationMapper.toDto(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.findByUser_UserIdAndIsReadFalse(userId)
                .forEach(n -> n.setIsRead(true));
    }
}
