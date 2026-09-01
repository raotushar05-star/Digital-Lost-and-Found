package com.lostandfound.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    /** Accepts either an email address or a registered mobile number. */
    private String email;
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;
}
