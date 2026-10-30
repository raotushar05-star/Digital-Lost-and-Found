package com.lostandfound.service;

import com.lostandfound.dto.custody.DisposeRequest;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.CaseStatus;
import com.lostandfound.entity.enums.ClaimStatus;
import com.lostandfound.entity.enums.CustodyStatus;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.security.StationAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/** Closes out property nobody claimed within the retention period. */
@Service
@RequiredArgsConstructor
public class DisposalService {

    private static final Set<ClaimStatus> LIVE_CLAIMS = EnumSet.of(
            ClaimStatus.PENDING, ClaimStatus.UNDER_VERIFICATION, ClaimStatus.DISPUTED, ClaimStatus.APPROVED);

    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final CaseService caseService;
    private final AuditService auditService;

    @Value("${app.retention.days:90}")
    private int retentionDays;

    @Transactional
    public void dispose(UUID foundItemId, User officer, DisposeRequest request) {
        FoundItem item = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found: " + foundItemId));
        StationAccess.assertOwnStation(officer, item.getStation());
        if (item.getCustodyStatus() != CustodyStatus.IN_CUSTODY) {
            throw new BadRequestException("Only items in custody can be disposed of");
        }
        long days = ChronoUnit.DAYS.between(item.getReceivedDate(), LocalDateTime.now());
        if (days < retentionDays) {
            throw new BadRequestException("This item has been in custody for " + days
                    + " day(s). It can be disposed of after " + retentionDays + " days");
        }
        boolean liveClaim = claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItemId).stream()
                .anyMatch(c -> LIVE_CLAIMS.contains(c.getStatus()));
        if (liveClaim) {
            throw new BadRequestException("This item has an open or approved claim. Resolve it first");
        }

        item.setCustodyStatus(CustodyStatus.DISPOSED);
        item.setDisposedAt(LocalDateTime.now());
        item.setDisposalMethod(request.getMethod());
        item.setDisposalNotes(request.getNotes());
        foundItemRepository.save(item);
        caseService.findAndTransition(item, CaseStatus.RESOLVED, officer,
                "Disposed after retention period (" + request.getMethod() + ")");
        auditService.log(officer, "FOUND_ITEM_DISPOSED", "FoundItem", foundItemId);
    }
}