package com.lostandfound.controller;

import com.lostandfound.dto.common.MessageResponse;
import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.dto.notification.NotificationDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public PagedResponse<NotificationDto> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.getMyNotifications(SecurityUtils.getCurrentUserId(), page, size);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationDto markRead(@PathVariable UUID notificationId) {
        return notificationService.markRead(SecurityUtils.getCurrentUserId(), notificationId);
    }

    @PatchMapping("/read-all")
    public MessageResponse markAllRead() {
        notificationService.markAllRead(SecurityUtils.getCurrentUserId());
        return MessageResponse.builder().message("All notifications marked as read").build();
    }
}
