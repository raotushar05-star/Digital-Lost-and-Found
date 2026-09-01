package com.lostandfound.dto.claim;

import com.lostandfound.dto.evidence.EvidenceDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClaimDetailDto {
    private UUID claimId;
    private UUID foundItemId;
    private String foundItemCategory;
    private String foundItemDescription;
    private UUID claimantId;
    private String claimantName;
    private UUID lostItemId;
    private String claimDetails;
    private String status;
    private UUID reviewedBy;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private List<EvidenceDto> evidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
