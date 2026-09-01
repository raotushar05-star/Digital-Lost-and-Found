package com.lostandfound.dto.reports;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StationReportDto {
    private UUID stationId;
    private String stationName;
    private long foundItems;
    private long verifiedItems;
    private long claims;
    private long recoveredItems;
    private double recoveryRate;
}
