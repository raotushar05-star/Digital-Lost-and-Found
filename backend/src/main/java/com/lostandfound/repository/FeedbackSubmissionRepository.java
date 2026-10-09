package com.lostandfound.repository;

import com.lostandfound.entity.FeedbackSubmission;
import com.lostandfound.entity.enums.FeedbackStatus;
import com.lostandfound.entity.enums.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedbackSubmissionRepository extends JpaRepository<FeedbackSubmission, UUID> {
    List<FeedbackSubmission> findByUser_UserIdOrderByCreatedAtDesc(UUID userId);
    List<FeedbackSubmission> findAllByOrderByCreatedAtDesc();
    List<FeedbackSubmission> findByStatusOrderByCreatedAtDesc(FeedbackStatus status);
    List<FeedbackSubmission> findByFeedbackTypeOrderByCreatedAtDesc(FeedbackType feedbackType);
    List<FeedbackSubmission> findByStatusAndFeedbackTypeOrderByCreatedAtDesc(FeedbackStatus status, FeedbackType feedbackType);
}