package com.lostandfound.service;

import com.lostandfound.dto.auth.*;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.ConflictException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.UserMapper;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.JwtTokenProvider;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.lostandfound.util.GoogleTokenVerifier;
import com.lostandfound.util.IdentityNormalizer;
import com.lostandfound.exception.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = IdentityNormalizer.email(request.getEmail());
        String phone = IdentityNormalizer.phone(request.getPhone());

        if (googleTokenVerifier.isEnabled()) {
            if (!StringUtils.hasText(request.getGoogleCredential())) {
                throw new BadRequestException("Please verify your email with Google first");
            }
            // The verified email from Google always wins over whatever was typed
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

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String raw = StringUtils.hasText(request.getEmail()) ? request.getEmail() : request.getPhone();
        if (!StringUtils.hasText(raw)) {
            throw new BadRequestException("Email or phone is required");
        }
        // Same cleaning as registration, so login still works after normalisation
        String identifier = raw.contains("@") ? IdentityNormalizer.email(raw) : IdentityNormalizer.phone(raw);
        User user = userRepository.findByEmailIgnoreCaseOrPhone(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("This account does not exist"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("This account has been deactivated");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
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
}
