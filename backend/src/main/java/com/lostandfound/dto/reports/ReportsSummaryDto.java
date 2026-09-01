package com.lostandfound.dto.reports;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportsSummaryDto {
    private long lostItems;
    private long foundItems;
    private long verifiedItems;
    private long claims;
    private long recoveredItems;
    private double recoveryRate;
}
