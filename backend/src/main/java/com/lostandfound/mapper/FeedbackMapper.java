package com.lostandfound.mapper;

import com.lostandfound.dto.feedback.FeedbackDto;
import com.lostandfound.entity.FeedbackSubmission;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public FeedbackDto toDto(FeedbackSubmission submission) {
        FeedbackDto dto = new FeedbackDto();
        dto.setFeedbackId(submission.getFeedbackId());
        dto.setUserId(submission.getUser().getUserId());
        dto.setUserName(submission.getUser().getName());
        dto.setUserEmail(submission.getUser().getEmail());
        dto.setUserPhone(submission.getUser().getPhone());
        dto.setFeedbackType(submission.getFeedbackType().name());
        dto.setSubject(submission.getSubject());
        dto.setMessage(submission.getMessage());
        dto.setStatus(submission.getStatus().name());
        dto.setAdminResponse(submission.getAdminResponse());
        dto.setRespondedByName(submission.getRespondedBy() != null ? submission.getRespondedBy().getName() : null);
        dto.setCreatedAt(submission.getCreatedAt());
        dto.setUpdatedAt(submission.getUpdatedAt());
        return dto;
    }
}