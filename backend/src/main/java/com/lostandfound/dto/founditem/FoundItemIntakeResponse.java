package com.lostandfound.dto.founditem;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoundItemIntakeResponse {
    private UUID foundItemId;
    private String status;
    private String message;
}
