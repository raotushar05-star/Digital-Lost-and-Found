package com.lostandfound.dto.handover;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HandoverCodeResponse {
    private String code;
    private LocalDateTime expiresAt;
}