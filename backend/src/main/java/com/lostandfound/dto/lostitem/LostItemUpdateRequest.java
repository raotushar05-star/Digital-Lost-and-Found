package com.lostandfound.dto.lostitem;

import com.lostandfound.dto.location.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LostItemUpdateRequest {

    @NotBlank(message = "Description is required")
    private String description;

    private String brand;
    private String color;
    private String identifyingDetails;

    @NotNull(message = "Lost date is required")
    @PastOrPresent(message = "Lost date cannot be in the future")
    private LocalDate lostDate;

    @Valid
    private LocationDto location;
}
