package com.lostandfound.dto.dispute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisputeUpdateRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "OPEN|UNDER_REVIEW|RESOLVED|CLOSED", message = "Invalid dispute status")
    private String status;

    private String resolution;
}
