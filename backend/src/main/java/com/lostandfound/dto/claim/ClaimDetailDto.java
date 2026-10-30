package com.lostandfound.dto.claim;

import com.lostandfound.dto.evidence.EvidenceDto;
import com.lostandfound.dto.file.PhotoDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private boolean handoverRecorded;
    /**
     * Photos from the claimant's own lost report. Filled for police viewers only.
     */
    private List<PhotoDto> lostItemPhotos;
}
