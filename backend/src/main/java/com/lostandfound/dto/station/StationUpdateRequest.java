package com.lostandfound.dto.station;

import com.lostandfound.dto.location.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StationUpdateRequest {

    @NotBlank(message = "Station name is required")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'&()-]{2,99}$",
            message = "Station name can contain only letters and spaces")
    private String stationName;

    @NotBlank(message = "Address is required")
    private String address;

    @Pattern(regexp = "^$|^[0-9]{8,12}$", message = "Station phone must be 8 to 12 digits only")
    private String phone;

    private Boolean isActive;

    @Valid
    private LocationDto location;
}