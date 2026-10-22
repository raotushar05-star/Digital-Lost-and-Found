package com.lostandfound.service;

import com.lostandfound.dto.admin.AdminUserDto;
import com.lostandfound.dto.common.PagedResponse;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PagedResponse<AdminUserDto> list(String q, Role role, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("name"));
        String query = q == null ? "" : q.trim();
        Page<User> result = role == null
                ? userRepository.search(query, pageable)
                : userRepository.searchByRole(query, role, pageable);
        return PagedResponse.of(result.map(this::toDto));
    }

    @Transactional
    public AdminUserDto setActive(UUID actorId, UUID targetId, boolean active) {
        User target = findTarget(actorId, targetId);
        target.setIsActive(active);
        userRepository.save(target);
        auditService.log(userRepository.findById(actorId).orElse(null),
                active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "User", targetId);
        return toDto(target);
    }

    @Transactional
    public AdminUserDto unlock(UUID actorId, UUID targetId) {
        User target = findTarget(actorId, targetId);
        target.setFailedLoginAttempts(0);
        target.setLockedUntil(null);
        userRepository.save(target);
        auditService.log(userRepository.findById(actorId).orElse(null), "USER_UNLOCKED", "User", targetId);
        return toDto(target);
    }

    private User findTarget(UUID actorId, UUID targetId) {
        if (actorId.equals(targetId)) {
            throw new BadRequestException("You cannot change your own account here");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + targetId));
        if (target.getRole() == Role.SYSTEM_ADMIN) {
            throw new ForbiddenException("System administrator accounts cannot be changed here");
        }
        return target;
    }

    private AdminUserDto toDto(User u) {
        return AdminUserDto.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole().name())
                .stationName(u.getStation() != null ? u.getStation().getStationName() : null)
                .isActive(u.getIsActive())
                .locked(u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now()))
                .createdAt(u.getCreatedAt())
                .build();
    }
}