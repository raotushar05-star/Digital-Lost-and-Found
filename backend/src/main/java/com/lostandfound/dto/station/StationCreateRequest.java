package com.lostandfound.dto.station;

import com.lostandfound.dto.location.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    /** Map pin is required so lost reports can be assigned to the nearest station. */
    @NotNull(message = "Station location is required")
    @Valid
    private LocationDto location;

    @NotBlank(message = "Police admin name is required")
    @Size(max = 100)
    private String adminName;

    @NotBlank(message = "Police admin email is required")
    @Email(message = "Police admin email must be valid")
    private String adminEmail;

    @NotBlank(message = "Police admin phone is required")
    @Pattern(regexp = "^[0-9+][0-9 -]{6,14}$", message = "Police admin phone is invalid")
    private String adminPhone;

    @NotBlank(message = "Police admin password is required")
    @Size(min = 8, message = "Police admin password must be at least 8 characters")
    private String adminPassword;
}
