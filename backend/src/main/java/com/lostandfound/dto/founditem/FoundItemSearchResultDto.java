package com.lostandfound.dto.founditem;

import com.lostandfound.dto.location.LocationDto;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoundItemSearchResultDto {
    private UUID foundItemId;
    private String category;
    private String description;
    private String color;
    private String brand;
    private LocalDate foundDate;
    private LocationDto location;
    private Double distanceKm;
    private String primaryPhotoUrl;
}
