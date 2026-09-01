package com.lostandfound.dto.auth;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSummaryDto {
    private UUID userId;
    private String name;
    private String role;
}
