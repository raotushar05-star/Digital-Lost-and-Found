package com.lostandfound.service;

import com.lostandfound.dto.dashboard.PoliceDashboardDto;
import com.lostandfound.entity.enums.*;
import com.lostandfound.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FoundReportRepository foundReportRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final ClaimDisputeRepository disputeRepository;
    private final HandoverRecordRepository handoverRecordRepository;

    public PoliceDashboardDto getPoliceDashboard() {
        long pendingFoundReports = foundReportRepository.countByStatus(FoundReportStatus.SUBMITTED);
        long pendingVerifications = foundItemRepository.countByVerificationStatus(FoundItemVerificationStatus.PENDING);
        long verifiedFoundItems = foundItemRepository.countByVerificationStatus(FoundItemVerificationStatus.VERIFIED);
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING) + claimRepository.countByStatus(ClaimStatus.UNDER_VERIFICATION);
        long itemsReturned = handoverRecordRepository.count();
        long openDisputes = disputeRepository.countByStatus(DisputeStatus.OPEN) + disputeRepository.countByStatus(DisputeStatus.UNDER_REVIEW);

        return PoliceDashboardDto.builder()
                .pendingFoundReports(pendingFoundReports)
                .pendingVerifications(pendingVerifications)
                .verifiedFoundItems(verifiedFoundItems)
                .pendingClaims(pendingClaims)
                .itemsReturned(itemsReturned)
                .openDisputes(openDisputes)
                .build();
    }
}
