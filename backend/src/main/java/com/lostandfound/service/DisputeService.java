package com.lostandfound.service;

import com.lostandfound.dto.dispute.DisputeCreateRequest;
import com.lostandfound.dto.dispute.DisputeDto;
import com.lostandfound.dto.dispute.DisputeUpdateRequest;
import com.lostandfound.entity.Claim;
import com.lostandfound.entity.ClaimDispute;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.CaseStatus;
import com.lostandfound.entity.enums.ClaimStatus;
import com.lostandfound.entity.enums.DisputeStatus;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.DisputeMapper;
import com.lostandfound.repository.ClaimDisputeRepository;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.FoundItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fraud Prevention & Dispute Module. A found item can carry multiple claims;
 * disputes give police a formal mechanism to flag and resolve competing or
 * suspicious claims rather than approving on a first-come basis.
 */
@Service
@RequiredArgsConstructor
public class DisputeService {

    private final ClaimDisputeRepository disputeRepository;
    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;
    private final CaseService caseService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final DisputeMapper disputeMapper;

    public List<DisputeDto> getDisputesForFoundItem(UUID foundItemId) {
        return disputeRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItemId).stream()
                .map(disputeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DisputeDto create(UUID foundItemId, User officer, DisputeCreateRequest request) {
        FoundItem foundItem = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found: " + foundItemId));
        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + request.getClaimId()));
        if (!claim.getFoundItem().getFoundItemId().equals(foundItemId)) {
            throw new BadRequestException("The referenced claim does not belong to this found item");
        }

        ClaimDispute dispute = ClaimDispute.builder()
                .foundItem(foundItem)
                .claim(claim)
                .raisedBy(officer)
                .reason(request.getReason())
                .status(DisputeStatus.OPEN)
                .build();
        dispute = disputeRepository.save(dispute);

        claim.setStatus(ClaimStatus.DISPUTED);
        claimRepository.save(claim);
        caseService.findAndTransition(foundItem, CaseStatus.UNDER_VERIFICATION, officer, "Dispute raised: " + request.getReason());

        notificationService.notify(claim.getClaimant(), NotificationType.DISPUTE_UPDATE,
                "A dispute has been raised on your claim",
                "Police are investigating competing or unclear ownership evidence for this item. You may be contacted for further information.",
                null, null);

        auditService.log(officer, "DISPUTE_RAISED", "ClaimDispute", dispute.getDisputeId());
        return disputeMapper.toDto(dispute);
    }

    @Transactional
    public DisputeDto update(UUID disputeId, User officer, DisputeUpdateRequest request) {
        ClaimDispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found: " + disputeId));

        DisputeStatus status;
        try {
            status = DisputeStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid dispute status: " + request.getStatus());
        }

        dispute.setStatus(status);
        dispute.setResolution(request.getResolution());
        if (status == DisputeStatus.RESOLVED || status == DisputeStatus.CLOSED) {
            dispute.setResolvedBy(officer);
            dispute.setResolvedAt(LocalDateTime.now());
            notificationService.notify(dispute.getClaim().getClaimant(), NotificationType.DISPUTE_UPDATE,
                    "The dispute on your claim has been resolved",
                    request.getResolution() != null ? request.getResolution() : "Police have completed their review. A final ownership decision will follow.",
                    null, null);
        }
        dispute = disputeRepository.save(dispute);
        auditService.log(officer, "DISPUTE_UPDATED", "ClaimDispute", dispute.getDisputeId());
        return disputeMapper.toDto(dispute);
    }
}
