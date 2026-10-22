package com.lostandfound.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForgotPasswordRequest {
    @NotBlank(message = "Email is required")
    private String email;
}