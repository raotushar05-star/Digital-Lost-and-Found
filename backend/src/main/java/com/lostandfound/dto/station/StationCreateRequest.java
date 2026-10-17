package com.lostandfound.dto.station;

import com.lostandfound.dto.location.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StationCreateRequest {

    @NotBlank(message = "Station name is required")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'&()-]{2,99}$",
            message = "Station name can contain only letters and spaces")
    private String stationName;

    @NotBlank(message = "Station code is required")
    @Pattern(regexp = "^[A-Z0-9-]{3,30}$",
            message = "Station code must be 3 to 30 characters: capital letters, numbers and hyphens")
    private String stationCode;

    @NotBlank(message = "Address is required")
    private String address;

    @Pattern(regexp = "^$|^[0-9]{8,12}$", message = "Station phone must be 8 to 12 digits only")
    private String phone;

    /** Map pin is required so lost reports can be assigned to the nearest station. */
    @NotNull(message = "Station location is required")
    @Valid
    private LocationDto location;

    @NotBlank(message = "Officer name is required")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]{1,99}$", message = "Officer name can contain only letters and spaces")
    private String officerName;

    @NotBlank(message = "Officer email is required")
    @Email(message = "Officer email must be valid")
    private String officerEmail;

    @NotBlank(message = "Officer phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Officer phone must be exactly 10 digits")
    private String officerPhone;

    @NotBlank(message = "Officer password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$",
            message = "Password must be 8 to 64 characters with an uppercase letter, a lowercase letter, a number and a symbol")
    private String officerPassword;
}