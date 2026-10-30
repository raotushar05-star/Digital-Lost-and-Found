package com.lostandfound.dto.handover;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HandoverRequest {

    @NotNull(message = "Recipient is required")
    private UUID recipientId;

    @NotBlank(message = "Handover code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Handover code must be 6 digits")
    private String otp;

    private String handoverNotes;
    private String acknowledgementReference;
}