package com.lostandfound.controller;

import com.lostandfound.dto.admin.AdminUserDto;
import com.lostandfound.dto.admin.SetActiveRequest;
import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public PagedResponse<AdminUserDto> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return adminUserService.list(q, role, page, size);
    }

    @PatchMapping("/{userId}/active")
    public AdminUserDto setActive(@PathVariable UUID userId, @Valid @RequestBody SetActiveRequest request) {
        return adminUserService.setActive(SecurityUtils.getCurrentUserId(), userId, request.getActive());
    }

    @PatchMapping("/{userId}/unlock")
    public AdminUserDto unlock(@PathVariable UUID userId) {
        return adminUserService.unlock(SecurityUtils.getCurrentUserId(), userId);
    }
}