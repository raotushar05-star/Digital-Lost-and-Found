package com.lostandfound.dto.evidence;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvidenceDto {
    private UUID evidenceId;
    private UUID claimId;
    private String evidenceType;
    private String description;
    private String fileUrl;
    private String verificationStatus;
    private UUID verifiedBy;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
