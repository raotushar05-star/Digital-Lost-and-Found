package com.lostandfound.dto.claim;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClaimCreateRequest {
    /** Optional: links the claim to the claimants own lost-item report. */
    private UUID lostItemId;

    @NotBlank(message = "Claim details are required")
    private String claimDetails;
}
