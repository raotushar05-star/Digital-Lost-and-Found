package com.lostandfound.dto.dashboard;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PoliceDashboardDto {
    private long pendingFoundReports;
    private long pendingVerifications;
    private long verifiedFoundItems;
    private long pendingClaims;
    private long itemsReturned;
    private long openDisputes;
}
