package com.lostandfound.dto.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyFoundItemRequest {

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "VERIFIED|REJECTED", message = "Decision must be VERIFIED or REJECTED")
    private String decision;

    private String verificationNotes;
}
