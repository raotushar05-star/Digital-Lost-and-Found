package com.lostandfound.dto.feedback;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
public class FeedbackDto {
    private UUID feedbackId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String feedbackType;
    private String subject;
    private String message;
    private String status;
    private String adminResponse;
    private String respondedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}