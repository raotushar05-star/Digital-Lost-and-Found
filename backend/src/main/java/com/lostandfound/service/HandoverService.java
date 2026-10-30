package com.lostandfound.service;

import com.lostandfound.dto.handover.HandoverCodeResponse;
import com.lostandfound.dto.handover.HandoverRequest;
import com.lostandfound.dto.handover.HandoverResponse;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.*;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ConflictException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.CaseRepository;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.FoundReportRepository;
import com.lostandfound.repository.HandoverRecordRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.StationAccess;
import com.lostandfound.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handover & Recovery Module. Physically closes out an approved claim.
 * Handover needs an APPROVED claim and the claimant's one-time code.
 */
@Service
@RequiredArgsConstructor
public class HandoverService {

    private static final int OTP_VALID_HOURS = 48;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final HandoverRecordRepository handoverRecordRepository;
    private final ClaimRepository claimRepository;
    private final FoundReportRepository foundReportRepository;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final CaseService caseService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final HandoverReceiptPdfService receiptPdfService;

    private final SecureRandom random = new SecureRandom();

    /** Claimant asks for a code to show the officer. Generating a new one cancels the old one. */
    @Transactional
    public HandoverCodeResponse issueCode(UUID claimId, UserPrincipal principal) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));
        if (!claim.getClaimant().getUserId().equals(principal.getUserId())) {
            throw new ForbiddenException("Only the claimant can generate the handover code");
        }
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("A handover code is available only after your claim is approved");
        }
        if (handoverRecordRepository.findByClaim_ClaimId(claimId).isPresent()) {
            throw new ConflictException("This item has already been handed over");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        claim.setHandoverOtpHash(passwordEncoder.encode(code));
        claim.setHandoverOtpExpiresAt(LocalDateTime.now().plusHours(OTP_VALID_HOURS));
        claim.setHandoverOtpAttempts(0);
        claimRepository.save(claim);
        auditService.log(claim.getClaimant(), "HANDOVER_CODE_ISSUED", "Claim", claimId);
        return HandoverCodeResponse.builder().code(code).expiresAt(claim.getHandoverOtpExpiresAt()).build();
    }

    /** noRollbackFor: a wrong code must still count as an attempt. */
    @Transactional(noRollbackFor = BadRequestException.class)
    public HandoverResponse recordHandover(UUID claimId, User officer, HandoverRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimId));
        StationAccess.assertOwnStation(officer, claim.getFoundItem().getStation());
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("Handover is only allowed after the claim has been approved");
        }
        if (handoverRecordRepository.findByClaim_ClaimId(claimId).isPresent()) {
            throw new ConflictException("A handover has already been recorded for this claim");
        }
        FoundItem foundItem = claim.getFoundItem();
        if (handoverRecordRepository.findByFoundItem_FoundItemId(foundItem.getFoundItemId()).isPresent()) {
            throw new ConflictException("This item has already been handed over to another claimant");
        }
        verifyCode(claim, request.getOtp());
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Recipient user not found: " + request.getRecipientId()));

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
        caseService.findAndTransition(foundItem, CaseStatus.RETURNED, officer,
                "Item physically handed over to recipient");
        caseService.findAndTransition(foundItem, CaseStatus.RESOLVED, officer,
                "Case resolved after successful handover");
        if (foundItem.getFoundReport() != null) {
            foundItem.getFoundReport().setStatus(FoundReportStatus.RETURNED);
            foundReportRepository.save(foundItem.getFoundReport());
        }

        if (claim.getLostItem() != null) {
            claim.getLostItem().setStatus(LostItemStatus.RESOLVED);
            caseService.findAndTransition(claim.getLostItem(), CaseStatus.RETURNED, officer,
                    "Item physically handed over to recipient");
            caseService.findAndTransition(claim.getLostItem(), CaseStatus.RESOLVED, officer,
                    "Case resolved after successful handover");
        }

        rejectRemainingClaims(foundItem, claim, officer);

        notificationService.notify(recipient, NotificationType.ITEM_RECOVERED,
                "Item handover completed",
                "Your item has been physically returned to you. Reference: " +
                        (request.getAcknowledgementReference() != null && !request.getAcknowledgementReference().isBlank()
                                ? request.getAcknowledgementReference()
                                : record.getHandoverId()),
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

    /** Receipt PDF for the claimant, or for police of the item's station. */
    @Transactional(readOnly = true)
    public CaseReportExportService.PdfFile receipt(UUID claimId, UserPrincipal principal) {
        HandoverRecord record = handoverRecordRepository.findByClaim_ClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("No handover has been recorded for this claim yet"));
        boolean isClaimant = record.getClaim().getClaimant().getUserId().equals(principal.getUserId());
        if (!isClaimant) {
            if (!StationAccess.isPoliceStaff(principal) && !StationAccess.isSystemAdmin(principal)) {
                throw new ForbiddenException("You do not have permission to view this receipt");
            }
            StationAccess.assertOwnStation(principal, record.getFoundItem().getStation());
        }
        Case linkedCase = caseRepository.findByFoundItem_FoundItemId(record.getFoundItem().getFoundItemId()).orElse(null);
        String number = linkedCase != null ? linkedCase.getCaseNumber() : claimId.toString().substring(0, 8);
        return new CaseReportExportService.PdfFile(
                "findline-receipt-" + number.replaceAll("[^\\w.-]+", "-") + ".pdf",
                receiptPdfService.render(record, linkedCase));
    }

    private void verifyCode(Claim claim, String code) {
        if (claim.getHandoverOtpHash() == null || claim.getHandoverOtpExpiresAt() == null
                || claim.getHandoverOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(
                    "No active handover code. Ask the claimant to open their claim and generate a new code");
        }
        int attempts = claim.getHandoverOtpAttempts() == null ? 0 : claim.getHandoverOtpAttempts();
        if (attempts >= MAX_OTP_ATTEMPTS) {
            throw new BadRequestException("Too many wrong codes. The claimant must generate a new code");
        }
        if (!passwordEncoder.matches(code, claim.getHandoverOtpHash())) {
            claim.setHandoverOtpAttempts(attempts + 1);
            claimRepository.save(claim);
            throw new BadRequestException("Incorrect handover code (" + (MAX_OTP_ATTEMPTS - attempts - 1) + " attempt(s) left)");
        }
        claim.setHandoverOtpHash(null);
        claim.setHandoverOtpExpiresAt(null);
        claim.setHandoverOtpAttempts(0);
        claimRepository.save(claim);
    }

    private void rejectRemainingClaims(FoundItem foundItem, Claim approvedClaim, User officer) {
        claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItem.getFoundItemId()).forEach(other -> {
            if (other.getClaimId().equals(approvedClaim.getClaimId()))
                return;
            if (other.getStatus() == ClaimStatus.PENDING || other.getStatus() == ClaimStatus.UNDER_VERIFICATION
                    || other.getStatus() == ClaimStatus.DISPUTED) {
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