package com.lostandfound.dto.custody;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisposeRequest {
    @NotBlank(message = "Disposal method is required")
    @Pattern(regexp = "AUCTIONED|DONATED|DESTROYED|HANDED_TO_AUTHORITY", message = "Invalid disposal method")
    private String method;

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;
}