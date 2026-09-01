package com.lostandfound.dto.claim;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClaimSummaryDto {
    private UUID claimId;
    private UUID foundItemId;
    private String foundItemCategory;
    private String foundItemDescription;
    private UUID claimantId;
    private String claimantName;
    private String status;
    private LocalDateTime createdAt;
}
