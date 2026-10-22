package com.lostandfound.service;

import com.lostandfound.dto.admin.AuditLogDto;
import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.entity.AuditLog;
import com.lostandfound.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> search(String action, String q, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PagedResponse.of(
                auditLogRepository.search(clean(action), clean(q), PageRequest.of(Math.max(page, 0), safeSize))
                        .map(this::toDto));
    }

    private AuditLogDto toDto(AuditLog a) {
        return AuditLogDto.builder()
                .auditId(a.getAuditId())
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .userName(a.getUser() != null ? a.getUser().getName() : null)
                .userEmail(a.getUser() != null ? a.getUser().getEmail() : null)
                .userRole(a.getUser() != null && a.getUser().getRole() != null ? a.getUser().getRole().name() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}