package com.lostandfound.dto.foundreport;

import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.util.ValidationPatterns;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoundReportCreateRequest {

    @NotNull(message = "Category is required")
    private UUID categoryId;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 1000, message = "Description must be 5 to 1000 characters")
    private String description;

    @Pattern(regexp = ValidationPatterns.BRAND, message = "Brand can contain letters, numbers and spaces only (max 100)")
    private String brand;

    @Pattern(regexp = ValidationPatterns.COLOR, message = "Colour can contain letters and spaces only (max 50)")
    private String color;

    @NotNull(message = "Found date is required")
    @PastOrPresent(message = "Found date cannot be in the future")
    private LocalDate foundDate;

    @NotNull(message = "Location is required")
    @Valid
    private LocationDto location;
}
