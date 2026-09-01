package com.lostandfound.dto.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileDto {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String role;
    private UUID stationId;
    private String stationName;
    private LocalDateTime createdAt;
}
