package com.lostandfound.dto.station;

import lombok.*;

@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class StationCreateResponse {
    private StationDto station;
    /** Login created for this station. The password is never returned. */
    private StationAdminCredentialsDto policeOfficer;
    private String message;
}