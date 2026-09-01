package com.lostandfound.service;

import com.lostandfound.dto.handover.HandoverRequest;
import com.lostandfound.dto.handover.HandoverResponse;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.*;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ConflictException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.HandoverRecordRepository;
import com.lostandfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handover & Recovery Module. Physically closes out an approved claim.
 * Per the frozen rule, handover is only permitted once a claim is APPROVED.
 */
@Service
@RequiredArgsConstructor
public class HandoverService {

    private final HandoverRecordRepository handoverRecordRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final CaseService caseService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public HandoverResponse recordHandover(UUID claimId, User officer, HandoverRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Handover is only allowed after the claim has been approved");
        }
        if (handoverRecordRepository.findByClaim_ClaimId(claimId).isPresent()) {
            throw new ConflictException("A handover has already been recorded for this claim");
        }
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found: " + request.getRecipientId()));

        FoundItem foundItem = claim.getFoundItem();

        HandoverRecord record = HandoverRecord.builder()
                .foundItem(foundItem)
                .claim(claim)
                .recipient(recipient)
                .officer(officer)
                .handoverNotes(request.getHandoverNotes())
                .acknowledgementReference(request.getAcknowledgementReference())
                .build();
        record = handoverRecordRepository.save(record);

        foundItem.setCustodyStatus(CustodyStatus.RETURNED);
        caseService.findAndTransition(foundItem, CaseStatus.RETURNED, officer, "Item physically handed over to recipient");
        caseService.findAndTransition(foundItem, CaseStatus.RESOLVED, officer, "Case resolved after successful handover");

        if (claim.getLostItem() != null) {
            claim.getLostItem().setStatus(LostItemStatus.RESOLVED);
            caseService.findAndTransition(claim.getLostItem(), CaseStatus.RETURNED, officer, "Item physically handed over to recipient");
            caseService.findAndTransition(claim.getLostItem(), CaseStatus.RESOLVED, officer, "Case resolved after successful handover");
        }

        rejectRemainingClaims(foundItem, claim, officer);

        notificationService.notify(recipient, NotificationType.ITEM_RECOVERED,
                "Item handover completed",
                "Your item has been physically returned to you. Reference: " +
                        (request.getAcknowledgementReference() != null ? request.getAcknowledgementReference() : record.getHandoverId()),
                null, null);

        auditService.log(officer, "ITEM_HANDED_OVER", "HandoverRecord", record.getHandoverId());

        return HandoverResponse.builder()
                .handoverId(record.getHandoverId())
                .foundItemId(foundItem.getFoundItemId())
                .claimId(claim.getClaimId())
                .handoverDate(record.getHandoverDate())
                .message("Item handover recorded successfully")
                .build();
    }

    private void rejectRemainingClaims(FoundItem foundItem, Claim approvedClaim, User officer) {
        claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItem.getFoundItemId()).forEach(other -> {
            if (other.getClaimId().equals(approvedClaim.getClaimId())) return;
            if (other.getStatus() == ClaimStatus.PENDING || other.getStatus() == ClaimStatus.UNDER_VERIFICATION || other.getStatus() == ClaimStatus.DISPUTED) {
                other.setStatus(ClaimStatus.REJECTED);
                claimRepository.save(other);
                notificationService.notify(other.getClaimant(), NotificationType.CLAIM_UPDATE,
                        "This item has already been returned",
                        "The item you claimed has already been handed over to another verified owner.",
                        null, null);
            }
        });
    }
}
