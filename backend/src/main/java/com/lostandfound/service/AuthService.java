package com.lostandfound.service;

import com.lostandfound.dto.auth.*;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.ConflictException;
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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("An account with this phone number already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null)
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .address(request.getAddress())
                .isActive(true)
                .build();
        user = userRepository.save(user);
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
        String identifier = StringUtils.hasText(request.getEmail()) ? request.getEmail() : request.getPhone();
        if (!StringUtils.hasText(identifier)) {
            throw new BadCredentialsException("Email or phone is required");
        }
        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
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
