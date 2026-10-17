package com.lostandfound.service;

import com.lostandfound.dto.casedto.CaseDto;
import com.lostandfound.dto.claim.ClaimSummaryDto;
import com.lostandfound.dto.foundreport.FoundReportDto;
import com.lostandfound.dto.lostitem.LostItemSummaryDto;
import com.lostandfound.dto.user.UpdateProfileRequest;
import com.lostandfound.dto.user.UserProfileDto;
import com.lostandfound.entity.Case;
import com.lostandfound.entity.Claim;
import com.lostandfound.entity.LostItem;
import com.lostandfound.entity.User;
import com.lostandfound.exception.ConflictException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.CaseMapper;
import com.lostandfound.mapper.ClaimMapper;
import com.lostandfound.mapper.FoundReportMapper;
import com.lostandfound.mapper.LostItemMapper;
import com.lostandfound.mapper.UserMapper;
import com.lostandfound.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.util.IdentityNormalizer;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundReportRepository foundReportRepository;
    private final ClaimRepository claimRepository;
    private final CaseRepository caseRepository;
    private final UserMapper userMapper;
    private final LostItemMapper lostItemMapper;
    private final FoundReportMapper foundReportMapper;
    private final ClaimMapper claimMapper;
    private final CaseMapper caseMapper;

    public User getEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    /** Resolves the full User entity for whoever is authenticated on the current request. */
    public User getCurrentUser() {
        return getEntityById(com.lostandfound.security.SecurityUtils.getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        return userMapper.toProfileDto(getEntityById(userId));
    }

        @Transactional
    public UserProfileDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getEntityById(userId);
        String email = IdentityNormalizer.email(request.getEmail());
        String phone = IdentityNormalizer.phone(request.getPhone());

        if (!IdentityNormalizer.isValidPhone(phone)) {
            throw new BadRequestException("Phone number is invalid");
        }
        if (email != null && !email.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        if (!phone.equals(user.getPhone()) && userRepository.existsByPhone(phone)) {
            throw new ConflictException("An account with this phone number already exists");
        }
        user.setName(request.getName().trim());
        user.setEmail(email);          // null, not "", when the field is left blank
        user.setPhone(phone);
        user.setAddress(request.getAddress());
        try {
            return userMapper.toProfileDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("An account with this email or phone number already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<LostItemSummaryDto> getMyLostItems(UUID userId) {
        return lostItemRepository.findByOwner_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(item -> lostItemMapper.toSummaryDto(item, caseRepository.findByLostItem_LostItemId(item.getLostItemId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FoundReportDto> getMyFoundReports(UUID userId) {
        return foundReportRepository.findByFinder_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(report -> foundReportMapper.toDto(report, null, java.util.List.of()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClaimSummaryDto> getMyClaims(UUID userId) {
        return claimRepository.findByClaimant_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(claimMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseDto> getMyCases(UUID userId) {
        Map<UUID, Case> cases = new LinkedHashMap<>();

        List<LostItem> lostItems = lostItemRepository.findByOwner_UserIdOrderByCreatedAtDesc(userId);
        for (LostItem item : lostItems) {
            caseRepository.findByLostItem_LostItemId(item.getLostItemId())
                    .ifPresent(c -> cases.put(c.getCaseId(), c));
        }

        List<Claim> claims = claimRepository.findByClaimant_UserIdOrderByCreatedAtDesc(userId);
        for (Claim claim : claims) {
            caseRepository.findByFoundItem_FoundItemId(claim.getFoundItem().getFoundItemId())
                    .ifPresent(c -> cases.put(c.getCaseId(), c));
        }

        return cases.values().stream().map(caseMapper::toDto).collect(Collectors.toList());
    }
}