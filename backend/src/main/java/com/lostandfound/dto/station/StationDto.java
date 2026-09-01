package com.lostandfound.dto.station;

import com.lostandfound.dto.location.LocationDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StationDto {
    private UUID stationId;
    private String stationName;
    private String stationCode;
    private String address;
    private String phone;
    private LocationDto location;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
