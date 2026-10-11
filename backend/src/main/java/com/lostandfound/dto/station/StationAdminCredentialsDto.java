package com.lostandfound.dto.station;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StationAdminCredentialsDto {
    private String name;
    private String email;
    private String phone;
    private String role;
}
