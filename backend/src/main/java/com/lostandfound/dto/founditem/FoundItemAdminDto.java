package com.lostandfound.dto.founditem;

import com.lostandfound.dto.file.PhotoDto;
import com.lostandfound.dto.location.LocationDto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full restricted representation of a found item, for authorized police personnel only.
 * Includes privateIdentifyingDetails used for later ownership verification.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoundItemAdminDto {
    private UUID foundItemId;
    private UUID foundReportId;
    private UUID stationId;
    private String stationName;
    private UUID categoryId;
    private String category;
    private String description;
    private String brand;
    private String color;
    private String privateIdentifyingDetails;
    private LocalDate foundDate;
    private LocalDateTime receivedDate;
    private LocationDto location;
    private String custodyStatus;
    private String verificationStatus;
    private UUID caseId;
    private String caseNumber;
    private List<PhotoDto> photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
