package com.lostandfound.controller;

import com.lostandfound.dto.feedback.FeedbackDto;
import com.lostandfound.dto.feedback.FeedbackUpdateRequest;
import com.lostandfound.entity.enums.FeedbackStatus;
import com.lostandfound.entity.enums.FeedbackType;
import com.lostandfound.service.FeedbackService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    @GetMapping
    public List<FeedbackDto> getAll(
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(required = false) FeedbackType type) {
        return feedbackService.getAll(status, type);
    }

    @PatchMapping("/{feedbackId}")
    public FeedbackDto respond(@PathVariable UUID feedbackId, @Valid @RequestBody FeedbackUpdateRequest request) {
        return feedbackService.respond(feedbackId, userService.getCurrentUser(), request);
    }
}