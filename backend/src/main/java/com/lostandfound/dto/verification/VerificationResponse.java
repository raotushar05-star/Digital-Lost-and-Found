package com.lostandfound.dto.verification;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationResponse {
    private UUID verificationId;
    private UUID foundItemId;
    private UUID claimId;
    private String decision;
    private LocalDateTime verifiedAt;
}
