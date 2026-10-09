package com.lostandfound.entity;

import com.lostandfound.entity.enums.FeedbackStatus;
import com.lostandfound.entity.enums.FeedbackType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackSubmission {

    @Id
    @GeneratedValue
    @Column(name = "feedback_id")
    private UUID feedbackId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 20)
    private FeedbackType feedbackType;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.OPEN;

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private User respondedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}