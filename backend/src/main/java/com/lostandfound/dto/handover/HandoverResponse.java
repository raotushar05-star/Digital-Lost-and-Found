package com.lostandfound.dto.handover;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HandoverResponse {
    private UUID handoverId;
    private UUID foundItemId;
    private UUID claimId;
    private LocalDateTime handoverDate;
    private String message;
}
