package com.lostandfound.service;

import com.lostandfound.dto.feedback.FeedbackCreateRequest;
import com.lostandfound.dto.feedback.FeedbackDto;
import com.lostandfound.dto.feedback.FeedbackUpdateRequest;
import com.lostandfound.entity.FeedbackSubmission;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.FeedbackStatus;
import com.lostandfound.entity.enums.FeedbackType;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.FeedbackMapper;
import com.lostandfound.repository.FeedbackSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackSubmissionRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public FeedbackDto create(User submitter, FeedbackCreateRequest request) {
        FeedbackSubmission submission = FeedbackSubmission.builder()
                .user(submitter)
                .feedbackType(request.getFeedbackType())
                .subject(request.getSubject().trim())
                .message(request.getMessage().trim())
                .status(FeedbackStatus.OPEN)
                .build();
        submission = feedbackRepository.save(submission);
        auditService.log(submitter, "FEEDBACK_SUBMITTED", "FeedbackSubmission", submission.getFeedbackId());
        return feedbackMapper.toDto(submission);
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> getMine(UUID userId) {
        return feedbackRepository.findByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(feedbackMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> getAll(FeedbackStatus status, FeedbackType type) {
        List<FeedbackSubmission> submissions;
        if (status != null && type != null) {
            submissions = feedbackRepository.findByStatusAndFeedbackTypeOrderByCreatedAtDesc(status, type);
        } else if (status != null) {
            submissions = feedbackRepository.findByStatusOrderByCreatedAtDesc(status);
        } else if (type != null) {
            submissions = feedbackRepository.findByFeedbackTypeOrderByCreatedAtDesc(type);
        } else {
            submissions = feedbackRepository.findAllByOrderByCreatedAtDesc();
        }
        return submissions.stream().map(feedbackMapper::toDto).toList();
    }

    @Transactional
    public FeedbackDto respond(UUID feedbackId, User admin, FeedbackUpdateRequest request) {
        FeedbackSubmission submission = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback submission not found: " + feedbackId));
        submission.setStatus(request.getStatus());
        submission.setAdminResponse(request.getAdminResponse().trim());
        submission.setRespondedBy(admin);
        submission = feedbackRepository.save(submission);

        auditService.log(admin, "FEEDBACK_RESPONDED", "FeedbackSubmission", submission.getFeedbackId());
        notificationService.notify(submission.getUser(), NotificationType.GENERAL,
                submission.getFeedbackType() == FeedbackType.COMPLAINT
                        ? "Update on your complaint" : "Response to your feedback",
                "Status: " + submission.getStatus().name().replace('_', ' ') + ". " + submission.getAdminResponse(),
                null, null);
        return feedbackMapper.toDto(submission);
    }
}