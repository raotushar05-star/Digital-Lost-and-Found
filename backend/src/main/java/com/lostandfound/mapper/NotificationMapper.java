package com.lostandfound.mapper;

import com.lostandfound.dto.notification.NotificationDto;
import com.lostandfound.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification n) {
        if (n == null) return null;
        return NotificationDto.builder()
                .notificationId(n.getNotificationId())
                .notificationType(n.getNotificationType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .relatedCaseId(n.getRelatedCase() != null ? n.getRelatedCase().getCaseId() : null)
                .relatedMatchId(n.getRelatedMatch() != null ? n.getRelatedMatch().getMatchId() : null)
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
