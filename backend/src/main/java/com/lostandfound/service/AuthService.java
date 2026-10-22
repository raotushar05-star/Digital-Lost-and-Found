package com.lostandfound.service;

import com.lostandfound.dto.auth.*;
import com.lostandfound.entity.PasswordResetToken;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ConflictException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.exception.TooManyAttemptsException;
import com.lostandfound.mapper.UserMapper;
import com.lostandfound.repository.PasswordResetTokenRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.JwtTokenProvider;
import com.lostandfound.util.GoogleTokenVerifier;
import com.lostandfound.util.IdentityNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;
    private static final int RESET_VALID_MINUTES = 30;
    private static final String FORGOT_MESSAGE = "If an account with that email exists, a reset link has been sent.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = IdentityNormalizer.email(request.getEmail());
        String phone = IdentityNormalizer.phone(request.getPhone());

        if (googleTokenVerifier.isEnabled()) {
            if (!StringUtils.hasText(request.getGoogleCredential())) {
                throw new BadRequestException("Please verify your email with Google first");
            }
            email = IdentityNormalizer.email(googleTokenVerifier.verify(request.getGoogleCredential()).email());
        }

        if (!IdentityNormalizer.isValidPhone(phone)) {
            throw new BadRequestException("Phone number is invalid");
        }
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new ConflictException("An account with this phone number already exists");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .address(request.getAddress())
                .isActive(true)
                .build();
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("An account with this email or phone number already exists");
        }
        auditService.log(user, "USER_REGISTERED", "User", user.getUserId());
        return RegisterResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .build();
    }

    /** noRollbackFor: the failed-attempt counter must still be saved when we throw. */
    @Transactional(noRollbackFor = {BadCredentialsException.class, TooManyAttemptsException.class})
    public AuthResponse login(LoginRequest request) {
        String raw = StringUtils.hasText(request.getEmail()) ? request.getEmail() : request.getPhone();
        if (!StringUtils.hasText(raw)) {
            throw new BadRequestException("Email or phone is required");
        }
        String identifier = raw.contains("@") ? IdentityNormalizer.email(raw) : IdentityNormalizer.phone(raw);
        User user = userRepository.findByEmailIgnoreCaseOrPhone(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        LocalDateTime now = LocalDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            long minutes = Math.max(1, Duration.between(now, user.getLockedUntil()).toMinutes() + 1);
            throw new TooManyAttemptsException("Too many failed attempts. Try again in " + minutes + " minute(s).");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("This account has been deactivated");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(now.plusMinutes(LOCK_MINUTES));
                userRepository.save(user);
                auditService.log(user, "ACCOUNT_LOCKED", "User", user.getUserId());
                throw new TooManyAttemptsException(
                        "Too many failed attempts. This account is locked for " + LOCK_MINUTES + " minutes.");
            }
            user.setFailedLoginAttempts(attempts);
            userRepository.save(user);
            auditService.log(user, "LOGIN_FAILED", "User", user.getUserId());
            throw new BadCredentialsException("Invalid credentials");
        }

        if ((user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getRole().name());
        auditService.log(user, "USER_LOGIN", "User", user.getUserId());
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationSeconds())
                .user(userMapper.toSummaryDto(user))
                .build();
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.log(user, "PASSWORD_CHANGED", "User", user.getUserId());
    }

    /** Always returns the same message, so it never reveals whether an email is registered. */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        String email = IdentityNormalizer.email(request.getEmail());
        if (email != null) {
            userRepository.findByEmailIgnoreCase(email)
                    .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                    .ifPresent(user -> {
                        resetTokenRepository.deleteByUser_UserId(user.getUserId());
                        byte[] bytes = new byte[32];
                        random.nextBytes(bytes);
                        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                        resetTokenRepository.save(PasswordResetToken.builder()
                                .user(user)
                                .tokenHash(sha256(rawToken))
                                .expiresAt(LocalDateTime.now().plusMinutes(RESET_VALID_MINUTES))
                                .build());
                        emailService.sendPasswordResetEmail(user, frontendUrl + "/reset-password?token=" + rawToken);
                        auditService.log(user, "PASSWORD_RESET_REQUESTED", "User", user.getUserId());
                    });
        }
        return FORGOT_MESSAGE;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = resetTokenRepository.findByTokenHash(sha256(request.getToken().trim()))
                .orElseThrow(() -> new BadRequestException("This reset link is invalid or has expired"));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This reset link is invalid or has expired");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        token.setUsedAt(LocalDateTime.now());
        resetTokenRepository.save(token);
        auditService.log(user, "PASSWORD_RESET_COMPLETED", "User", user.getUserId());
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}