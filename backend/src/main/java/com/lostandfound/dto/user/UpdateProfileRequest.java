package com.lostandfound.dto.user;

import com.lostandfound.util.ValidationPatterns;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Pattern(regexp = ValidationPatterns.PERSON_NAME, message = ValidationPatterns.PERSON_NAME_MSG)
    private String name;

    @Size(max = 150, message = "Email is too long")
    @Pattern(regexp = ValidationPatterns.EMAIL_OPTIONAL, message = ValidationPatterns.EMAIL_MSG)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationPatterns.PHONE_10, message = ValidationPatterns.PHONE_MSG)
    private String phone;

    @Size(max = 250, message = "Address must be at most 250 characters")
    private String address;
}
