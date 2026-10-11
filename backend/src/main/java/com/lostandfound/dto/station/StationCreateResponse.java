package com.lostandfound.dto.station;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StationCreateResponse {
    private StationDto station;
    /** Login created for this station; password is not returned (use the one you entered). */
    private StationAdminCredentialsDto policeAdmin;
    private String message;
}
