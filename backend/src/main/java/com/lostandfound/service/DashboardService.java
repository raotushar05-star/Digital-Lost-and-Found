package com.lostandfound.service;

import java.util.UUID;
import com.lostandfound.dto.dashboard.PoliceDashboardDto;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.security.UserPrincipal;
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
    private final LostItemRepository lostItemRepository;

    public PoliceDashboardDto getPoliceDashboard(UserPrincipal principal) {
        if (principal.getStationId() == null) {
            throw new ForbiddenException("Your police account is not assigned to a station");
        }
        UUID stationId = principal.getStationId();
        long pendingFoundReports = foundReportRepository
                .countByStatusAndAssignedStation_StationId(FoundReportStatus.SUBMITTED, principal.getStationId())
                + foundReportRepository.countByStatusAndAssignedStationIsNull(FoundReportStatus.SUBMITTED);
        long pendingLostReports = lostItemRepository.countByStatusAndAssignedStation_StationId(LostItemStatus.REPORTED,
                principal.getStationId())
                + lostItemRepository.countByStatusAndAssignedStationIsNull(LostItemStatus.REPORTED);
        long pendingVerifications = foundItemRepository.countByStation_StationIdAndVerificationStatus(stationId,
                FoundItemVerificationStatus.PENDING);
        long verifiedFoundItems = foundItemRepository.countByStation_StationIdAndVerificationStatus(stationId,
                FoundItemVerificationStatus.VERIFIED);
        long pendingClaims = claimRepository.countByFoundItem_Station_StationIdAndStatus(stationId, ClaimStatus.PENDING)
                + claimRepository.countByFoundItem_Station_StationIdAndStatus(stationId,
                        ClaimStatus.UNDER_VERIFICATION);
        long itemsReturned = handoverRecordRepository.countByFoundItem_Station_StationId(stationId);
        long openDisputes = disputeRepository.countByFoundItem_Station_StationIdAndStatus(stationId, DisputeStatus.OPEN)
                + disputeRepository.countByFoundItem_Station_StationIdAndStatus(stationId, DisputeStatus.UNDER_REVIEW);

        return PoliceDashboardDto.builder()
                .pendingFoundReports(pendingFoundReports)
                .pendingLostReports(pendingLostReports)
                .pendingVerifications(pendingVerifications)
                .verifiedFoundItems(verifiedFoundItems)
                .pendingClaims(pendingClaims)
                .itemsReturned(itemsReturned)
                .openDisputes(openDisputes)
                .build();
    }
}
