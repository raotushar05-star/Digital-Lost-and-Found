package com.lostandfound.dto.dispute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisputeCreateRequest {

    @NotNull(message = "Claim is required")
    private UUID claimId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
