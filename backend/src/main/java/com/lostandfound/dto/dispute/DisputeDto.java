package com.lostandfound.dto.dispute;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisputeDto {
    private UUID disputeId;
    private UUID foundItemId;
    private UUID claimId;
    private UUID raisedBy;
    private String raisedByName;
    private String reason;
    private String status;
    private String resolution;
    private UUID resolvedBy;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
