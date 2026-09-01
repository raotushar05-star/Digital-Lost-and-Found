package com.lostandfound.dto.casedto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CaseDto {
    private UUID caseId;
    private String caseNumber;
    private String caseType;
    private String currentStatus;
    private UUID lostItemId;
    private UUID foundItemId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
