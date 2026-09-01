package com.lostandfound.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostandfound.entity.AuditLog;
import com.lostandfound.entity.User;
import com.lostandfound.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Writes append-only accountability records to audit_logs.
 * Distinct from case_status_history: audit logs track system/security-relevant
 * actions (who did what to which entity), not the case-progression timeline.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public void log(User actor, String action, String entityType, UUID entityId, Object oldValue, Object newValue) {
        try {
            AuditLog log = AuditLog.builder()
                    .user(actor)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ex) {
            // Auditing must never block the primary business operation.
        }
    }

    public void log(User actor, String action, String entityType, UUID entityId) {
        log(actor, action, entityType, entityId, null, null);
    }
}
