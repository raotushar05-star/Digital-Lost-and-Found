package com.lostandfound.dto.auth;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterResponse {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String role;
}
