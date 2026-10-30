package com.lostandfound.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;
import com.lostandfound.util.ValidationPatterns;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = ValidationPatterns.PERSON_NAME_MSG)
    private String name;

    @Size(max = 150, message = "Email is too long")
    @Pattern(regexp = ValidationPatterns.EMAIL_OPTIONAL, message = ValidationPatterns.EMAIL_MSG)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationPatterns.PHONE_10, message = ValidationPatterns.PHONE_MSG)
    private String phone;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = ValidationPatterns.PASSWORD, message = ValidationPatterns.PASSWORD_MSG)
    private String password;

    @Size(max = 250, message = "Address must be at most 250 characters")
    private String address;

    /**
     * Google ID token proving ownership of the email. Required when Google sign-up
     * is enabled.
     */
    private String googleCredential;
}
