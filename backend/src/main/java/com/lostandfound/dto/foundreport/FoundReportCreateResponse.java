package com.lostandfound.dto.foundreport;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoundReportCreateResponse {
    private UUID foundReportId;
    private String status;
    private String message;
}
