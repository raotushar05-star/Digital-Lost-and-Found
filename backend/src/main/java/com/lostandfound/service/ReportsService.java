package com.lostandfound.service;

import com.lostandfound.dto.reports.ReportsSummaryDto;
import com.lostandfound.dto.reports.StationReportDto;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.HandoverRecord;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.entity.enums.FoundItemVerificationStatus;
import com.lostandfound.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Reports & Analytics Module. */
@Service
@RequiredArgsConstructor
public class ReportsService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final HandoverRecordRepository handoverRecordRepository;
    private final PoliceStationRepository stationRepository;

    public ReportsSummaryDto getSummary(UUID stationId, LocalDate dateFrom, LocalDate dateTo) {
        List<FoundItem> foundItems = foundItemRepository.findAll().stream()
                .filter(fi -> stationId == null || fi.getStation().getStationId().equals(stationId))
                .filter(fi -> inRange(fi.getCreatedAt(), dateFrom, dateTo))
                .collect(Collectors.toList());

        long lostItems = lostItemRepository.findAll().stream()
                .filter(li -> inRange(li.getCreatedAt(), dateFrom, dateTo))
                .count();

        long verifiedItems = foundItems.stream().filter(fi -> fi.getVerificationStatus() == FoundItemVerificationStatus.VERIFIED).count();

        long claims = claimRepository.findAll().stream()
                .filter(c -> stationId == null || c.getFoundItem().getStation().getStationId().equals(stationId))
                .filter(c -> inRange(c.getCreatedAt(), dateFrom, dateTo))
                .count();

        long recoveredItems = handoverRecordRepository.findAll().stream()
                .filter(h -> stationId == null || h.getFoundItem().getStation().getStationId().equals(stationId))
                .filter(h -> inRange(h.getHandoverDate(), dateFrom, dateTo))
                .count();

        double recoveryRate = foundItems.isEmpty() ? 0.0 : round2((double) recoveredItems / foundItems.size() * 100.0);

        return ReportsSummaryDto.builder()
                .lostItems(lostItems)
                .foundItems(foundItems.size())
                .verifiedItems(verifiedItems)
                .claims(claims)
                .recoveredItems(recoveredItems)
                .recoveryRate(recoveryRate)
                .build();
    }

    public List<StationReportDto> getStationReports(LocalDate dateFrom, LocalDate dateTo) {
        List<PoliceStation> stations = stationRepository.findByIsActiveTrue();
        List<FoundItem> allFoundItems = foundItemRepository.findAll();
        List<HandoverRecord> allHandovers = handoverRecordRepository.findAll();

        return stations.stream().map(station -> {
            List<FoundItem> stationItems = allFoundItems.stream()
                    .filter(fi -> fi.getStation().getStationId().equals(station.getStationId()))
                    .filter(fi -> inRange(fi.getCreatedAt(), dateFrom, dateTo))
                    .collect(Collectors.toList());
            long verified = stationItems.stream().filter(fi -> fi.getVerificationStatus() == FoundItemVerificationStatus.VERIFIED).count();
            long claims = claimRepository.findAll().stream()
                    .filter(c -> c.getFoundItem().getStation().getStationId().equals(station.getStationId()))
                    .filter(c -> inRange(c.getCreatedAt(), dateFrom, dateTo))
                    .count();
            long recovered = allHandovers.stream()
                    .filter(h -> h.getFoundItem().getStation().getStationId().equals(station.getStationId()))
                    .filter(h -> inRange(h.getHandoverDate(), dateFrom, dateTo))
                    .count();
            double recoveryRate = stationItems.isEmpty() ? 0.0 : round2((double) recovered / stationItems.size() * 100.0);

            return StationReportDto.builder()
                    .stationId(station.getStationId())
                    .stationName(station.getStationName())
                    .foundItems(stationItems.size())
                    .verifiedItems(verified)
                    .claims(claims)
                    .recoveredItems(recovered)
                    .recoveryRate(recoveryRate)
                    .build();
        }).collect(Collectors.toList());
    }

    private boolean inRange(LocalDateTime timestamp, LocalDate from, LocalDate to) {
        if (timestamp == null) return true;
        LocalDate date = timestamp.toLocalDate();
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
