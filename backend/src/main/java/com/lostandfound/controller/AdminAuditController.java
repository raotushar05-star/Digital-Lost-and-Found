package com.lostandfound.controller;

import com.lostandfound.dto.admin.AuditLogDto;
import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    public PagedResponse<AuditLogDto> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return auditLogService.search(action, q, page, size);
    }
}