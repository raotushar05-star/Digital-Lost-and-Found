package com.lostandfound.dto.lostitem;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LostItemCreateResponse {
    private UUID lostItemId;
    private String status;
    private String message;
}
