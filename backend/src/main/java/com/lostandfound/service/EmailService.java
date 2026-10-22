package com.lostandfound.service;

import com.lostandfound.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Email is a best-effort secondary channel alongside in-app notifications.
 * A failure here must never affect the business transaction that triggered it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async("emailTaskExecutor")
    public void sendNotificationEmail(User user, String subject, String body) {
        if (!mailEnabled) {
            return;
        }
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromAddress + ">");
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body + "\n\n---\nThis is an automated message from " + fromName
                    + ". Sign in to your account for full details.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send notification email to {}: {}", user.getEmail(), ex.getMessage());
        }
    }

    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(User user, String link) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            return;
        }
        if (!mailEnabled) {
            // Development convenience only. Remove this log line before any real deployment.
            log.warn("Mail is disabled. Password reset link for {}: {}", user.getEmail(), link);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromAddress + ">");
            message.setTo(user.getEmail());
            message.setSubject("Reset your " + fromName + " password");
            message.setText("We received a request to reset your password.\n\n"
                    + "Open this link within 30 minutes to choose a new one:\n" + link
                    + "\n\nIf you did not ask for this, you can ignore this email.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send password reset email to {}: {}", user.getEmail(), ex.getMessage());
        }
    }
}