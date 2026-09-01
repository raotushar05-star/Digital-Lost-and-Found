package com.lostandfound.dto.claim;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClaimCreateResponse {
    private UUID claimId;
    private String status;
    private String message;
}
