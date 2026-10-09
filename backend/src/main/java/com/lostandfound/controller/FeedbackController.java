package com.lostandfound.controller;

import com.lostandfound.dto.feedback.FeedbackCreateRequest;
import com.lostandfound.dto.feedback.FeedbackDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.FeedbackService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','POLICE_OFFICER','POLICE_ADMIN')")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    @GetMapping
    public List<FeedbackDto> getMine() {
        return feedbackService.getMine(SecurityUtils.getCurrentUserId());
    }

    @PostMapping
    public FeedbackDto create(@Valid @RequestBody FeedbackCreateRequest request) {
        return feedbackService.create(userService.getCurrentUser(), request);
    }
}