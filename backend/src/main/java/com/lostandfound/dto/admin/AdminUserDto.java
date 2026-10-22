package com.lostandfound.dto.admin;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUserDto {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String stationName;
    private Boolean isActive;
    private Boolean locked;
    private LocalDateTime createdAt;
}