package com.lostandfound.service;

import com.lostandfound.dto.verification.VerificationResponse;
import com.lostandfound.dto.verification.VerifyClaimRequest;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.*;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.VerificationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Ownership Verification Module. Approval must rest on the overall evidence
 * and police judgement, not merely possession of a receipt. A found item can
 * carry multiple competing claims; approving one automatically closes out the
 * others so custody is never ambiguous.
 */
@Service
@RequiredArgsConstructor
public class OwnershipVerificationService {

    private final ClaimRepository claimRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final CaseService caseService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public VerificationResponse verify(UUID claimId, User officer, VerifyClaimRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new com.lostandfound.exception.ResourceNotFoundException("Claim not found: " + claimId));
        if (claim.getStatus() == ClaimStatus.APPROVED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new BadRequestException("This claim has already been finalized (" + claim.getStatus() + ")");
        }

        VerificationDecision decision;
        try {
            decision = VerificationDecision.valueOf(request.getDecision());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Decision must be APPROVED or REJECTED");
        }

        VerificationRecord record = VerificationRecord.builder()
                .claim(claim)
                .officer(officer)
                .verificationType(VerificationType.OWNERSHIP_VERIFICATION)
                .decision(decision)
                .verificationNotes(request.getVerificationNotes())
                .build();
        record = verificationRecordRepository.save(record);

        claim.setReviewedBy(officer);
        claim.setReviewedAt(LocalDateTime.now());

        FoundItem foundItem = claim.getFoundItem();

        if (decision == VerificationDecision.APPROVED) {
            claim.setStatus(ClaimStatus.APPROVED);
            claimRepository.save(claim);

            foundItem.setCustodyStatus(CustodyStatus.CLAIMED);
            caseService.findAndTransition(foundItem, CaseStatus.APPROVED, officer, "Ownership verified and claim approved");
            if (claim.getLostItem() != null) {
                claim.getLostItem().setStatus(LostItemStatus.APPROVED);
                caseService.findAndTransition(claim.getLostItem(), CaseStatus.APPROVED, officer, "Ownership verified and claim approved");
            }

            notificationService.notify(claim.getClaimant(), NotificationType.CLAIM_UPDATE,
                    "Your claim has been approved",
                    "Police have verified your ownership claim. Please visit the station to complete the handover.",
                    null, null);

            rejectCompetingClaims(claim, officer);
        } else {
            claim.setStatus(ClaimStatus.REJECTED);
            claimRepository.save(claim);

            boolean otherActiveClaims = claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItem.getFoundItemId())
                    .stream()
                    .anyMatch(c -> !c.getClaimId().equals(claim.getClaimId())
                            && (c.getStatus() == ClaimStatus.PENDING || c.getStatus() == ClaimStatus.UNDER_VERIFICATION));

            if (!otherActiveClaims) {
                caseService.findAndTransition(foundItem, CaseStatus.POLICE_VERIFIED, officer, "Claim rejected; item remains available for other claimants");
            }
            if (claim.getLostItem() != null) {
                claim.getLostItem().setStatus(LostItemStatus.POTENTIAL_MATCH);
                caseService.findAndTransition(claim.getLostItem(), CaseStatus.POTENTIAL_MATCH, officer, "Claim rejected; report remains open");
            }

            notificationService.notify(claim.getClaimant(), NotificationType.CLAIM_UPDATE,
                    "Your claim was not approved",
                    "Police reviewed your ownership claim and were unable to verify it. " +
                            (request.getVerificationNotes() != null ? request.getVerificationNotes() : ""),
                    null, null);
        }

        auditService.log(officer, "OWNERSHIP_VERIFICATION_DECISION", "Claim", claim.getClaimId());

        return VerificationResponse.builder()
                .verificationId(record.getVerificationId())
                .claimId(claim.getClaimId())
                .decision(decision.name())
                .verifiedAt(record.getVerifiedAt())
                .build();
    }

    private void rejectCompetingClaims(Claim approvedClaim, User officer) {
        List<Claim> others = claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(approvedClaim.getFoundItem().getFoundItemId());
        for (Claim other : others) {
            if (other.getClaimId().equals(approvedClaim.getClaimId())) continue;
            if (other.getStatus() == ClaimStatus.PENDING || other.getStatus() == ClaimStatus.UNDER_VERIFICATION || other.getStatus() == ClaimStatus.DISPUTED) {
                other.setStatus(ClaimStatus.REJECTED);
                other.setReviewedBy(officer);
                other.setReviewedAt(LocalDateTime.now());
                claimRepository.save(other);
                notificationService.notify(other.getClaimant(), NotificationType.CLAIM_UPDATE,
                        "Your claim was not approved",
                        "Another claimant's ownership of this item was verified by police.",
                        null, null);
                auditService.log(officer, "CLAIM_AUTO_REJECTED_COMPETING", "Claim", other.getClaimId());
            }
        }
    }
}
