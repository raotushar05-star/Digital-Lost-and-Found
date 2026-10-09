package com.lostandfound.service;

import com.lostandfound.dto.feedback.FeedbackCreateRequest;
import com.lostandfound.dto.feedback.FeedbackUpdateRequest;
import com.lostandfound.entity.FeedbackSubmission;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.FeedbackStatus;
import com.lostandfound.entity.enums.FeedbackType;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.mapper.FeedbackMapper;
import com.lostandfound.repository.FeedbackSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackSubmissionRepository feedbackRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    private final FeedbackMapper feedbackMapper = new FeedbackMapper();

    @Test
    void create_savesPrivateSubmissionAsOpen() {
        FeedbackService service = new FeedbackService(feedbackRepository, feedbackMapper, notificationService, auditService);
        User submitter = user("Citizen", Role.USER);
        FeedbackCreateRequest request = new FeedbackCreateRequest();
        request.setFeedbackType(FeedbackType.COMPLAINT);
        request.setSubject("Station intake issue");
        request.setMessage("My report is assigned to the wrong station.");
        UUID feedbackId = UUID.randomUUID();
        when(feedbackRepository.save(any(FeedbackSubmission.class))).thenAnswer(invocation -> {
            FeedbackSubmission saved = invocation.getArgument(0);
            saved.setFeedbackId(feedbackId);
            return saved;
        });

        var result = service.create(submitter, request);

        ArgumentCaptor<FeedbackSubmission> captor = ArgumentCaptor.forClass(FeedbackSubmission.class);
        verify(feedbackRepository).save(captor.capture());
        assertEquals(submitter, captor.getValue().getUser());
        assertEquals(FeedbackType.COMPLAINT, captor.getValue().getFeedbackType());
        assertEquals(FeedbackStatus.OPEN, captor.getValue().getStatus());
        assertEquals("Station intake issue", result.getSubject());
        verify(auditService).log(submitter, "FEEDBACK_SUBMITTED", "FeedbackSubmission", feedbackId);
    }

    @Test
    void respond_updatesStatusAndNotifiesOriginalSubmitter() {
        FeedbackService service = new FeedbackService(feedbackRepository, feedbackMapper, notificationService, auditService);
        User submitter = user("Citizen", Role.USER);
        User admin = user("System Admin", Role.SYSTEM_ADMIN);
        FeedbackSubmission submission = FeedbackSubmission.builder()
                .feedbackId(UUID.randomUUID())
                .user(submitter)
                .feedbackType(FeedbackType.COMPLAINT)
                .subject("Station intake issue")
                .message("My report is assigned to the wrong station.")
                .status(FeedbackStatus.OPEN)
                .build();
        FeedbackUpdateRequest request = new FeedbackUpdateRequest();
        request.setStatus(FeedbackStatus.IN_REVIEW);
        request.setAdminResponse("We are reviewing the station assignment.");
        when(feedbackRepository.findById(submission.getFeedbackId())).thenReturn(Optional.of(submission));
        when(feedbackRepository.save(any(FeedbackSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.respond(submission.getFeedbackId(), admin, request);

        assertEquals(FeedbackStatus.IN_REVIEW, submission.getStatus());
        assertEquals("We are reviewing the station assignment.", submission.getAdminResponse());
        assertEquals(admin, submission.getRespondedBy());
        assertEquals("We are reviewing the station assignment.", result.getAdminResponse());
        verify(notificationService).notify(
                eq(submitter), eq(NotificationType.GENERAL), eq("Update on your complaint"),
                eq("Status: IN REVIEW. We are reviewing the station assignment."), isNull(), isNull());
    }

    private User user(String name, Role role) {
        return User.builder()
                .userId(UUID.randomUUID())
                .name(name)
                .email(name.toLowerCase().replace(' ', '.') + "@example.test")
                .phone(role == Role.USER ? "1234567890" : "1234567891")
                .passwordHash("hashed")
                .role(role)
                .isActive(true)
                .build();
    }
}
