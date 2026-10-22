package com.lostandfound.dto.admin;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogDto {
    private UUID auditId;
    private String action;
    private String entityType;
    private UUID entityId;
    private String userName;
    private String userEmail;
    private String userRole;
    private LocalDateTime createdAt;
}