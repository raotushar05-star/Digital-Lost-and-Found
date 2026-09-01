package com.lostandfound.dto.handover;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HandoverRequest {

    @NotNull(message = "Recipient is required")
    private UUID recipientId;

    private String handoverNotes;
    private String acknowledgementReference;
}
