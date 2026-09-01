package com.lostandfound.dto.notification;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationDto {
    private UUID notificationId;
    private String notificationType;
    private String title;
    private String message;
    private UUID relatedCaseId;
    private UUID relatedMatchId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
