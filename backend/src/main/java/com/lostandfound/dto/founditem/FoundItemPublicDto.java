package com.lostandfound.dto.founditem;

import com.lostandfound.dto.file.PhotoDto;
import com.lostandfound.dto.location.LocationDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Public-safe representation of a found item. Never includes
 * privateIdentifyingDetails, per the frozen data-visibility rules.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoundItemPublicDto {
    private UUID foundItemId;
    private String category;
    private String description;
    private String color;
    private String brand;
    private LocalDate foundDate;
    private LocationDto location;
    private String verificationStatus;
    private List<PhotoDto> photos;
}
