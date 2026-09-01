package com.lostandfound.dto.station;

import com.lostandfound.dto.location.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StationCreateRequest {

    @NotBlank(message = "Station name is required")
    private String stationName;

    @NotBlank(message = "Station code is required")
    private String stationCode;

    @NotBlank(message = "Address is required")
    private String address;

    private String phone;

    @Valid
    private LocationDto location;
}
