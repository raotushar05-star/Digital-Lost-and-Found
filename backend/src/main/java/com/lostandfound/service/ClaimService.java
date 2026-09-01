package com.lostandfound.service;

import com.lostandfound.dto.claim.ClaimCreateRequest;
import com.lostandfound.dto.claim.ClaimCreateResponse;
import com.lostandfound.dto.claim.ClaimDetailDto;
import com.lostandfound.dto.claim.ClaimSummaryDto;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.CaseStatus;
import com.lostandfound.entity.enums.ClaimStatus;
import com.lostandfound.entity.enums.FoundItemVerificationStatus;
import com.lostandfound.entity.enums.LostItemStatus;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.ClaimMapper;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.repository.LostItemRepository;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Claim Management Module. A match never proves ownership on its own -
 * a claim, evidence, and an explicit police verification decision are required.
 */
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;
    private final LostItemRepository lostItemRepository;
    private final CaseService caseService;
    private final AuditService auditService;
    private final ClaimMapper claimMapper;

    @Transactional
    public ClaimCreateResponse create(UUID foundItemId, User claimant, ClaimCreateRequest request) {
        FoundItem foundItem = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found: " + foundItemId));
        if (foundItem.getVerificationStatus() != FoundItemVerificationStatus.VERIFIED) {
            throw new BadRequestException("This item has not yet been verified by police and cannot be claimed");
        }

        LostItem lostItem = null;
        if (request.getLostItemId() != null) {
            lostItem = lostItemRepository.findById(request.getLostItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lost item not found: " + request.getLostItemId()));
            if (!lostItem.getOwner().getUserId().equals(claimant.getUserId())) {
                throw new ForbiddenException("You can only reference your own lost-item reports in a claim");
            }
        }

        Claim claim = Claim.builder()
                .foundItem(foundItem)
                .claimant(claimant)
                .lostItem(lostItem)
                .claimDetails(request.getClaimDetails())
                .status(ClaimStatus.PENDING)
                .build();
        claim = claimRepository.save(claim);

        caseService.findAndTransition(foundItem, CaseStatus.CLAIM_SUBMITTED, claimant, "Ownership claim submitted");
        if (lostItem != null) {
            lostItem.setStatus(LostItemStatus.CLAIM_SUBMITTED);
            lostItemRepository.save(lostItem);
            caseService.findAndTransition(lostItem, CaseStatus.CLAIM_SUBMITTED, claimant, "Ownership claim submitted");
        }

        auditService.log(claimant, "CLAIM_SUBMITTED", "Claim", claim.getClaimId());

        return ClaimCreateResponse.builder()
                .claimId(claim.getClaimId())
                .status(claim.getStatus().name())
                .message("Claim submitted successfully")
                .build();
    }

    public ClaimDetailDto getById(UUID claimId, UserPrincipal principal) {
        Claim claim = getEntityById(claimId);
        assertCanView(claim, principal);
        return claimMapper.toDetailDto(claim);
    }

    public List<ClaimSummaryDto> getClaimsForFoundItem(UUID foundItemId, UserPrincipal principal) {
        assertPolice(principal);
        return claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItemId).stream()
                .map(claimMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    public Claim getEntityById(UUID claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));
    }

    public void assertCanView(Claim claim, UserPrincipal principal) {
        boolean isClaimant = claim.getClaimant().getUserId().equals(principal.getUserId());
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (!isClaimant && !isPolice) {
            throw new ForbiddenException("You do not have permission to view this claim");
        }
    }

    private void assertPolice(UserPrincipal principal) {
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (!isPolice) {
            throw new ForbiddenException("This action is restricted to police personnel");
        }
    }
}
