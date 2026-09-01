package com.lostandfound.dto.lostitem;

import com.lostandfound.dto.file.PhotoDto;
import com.lostandfound.dto.location.LocationDto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LostItemDetailDto {
    private UUID lostItemId;
    private UUID ownerId;
    private String ownerName;
    private UUID categoryId;
    private String category;
    private String description;
    private String brand;
    private String color;
    private String identifyingDetails;
    private LocalDate lostDate;
    private LocationDto location;
    private String status;
    private UUID caseId;
    private String caseNumber;
    private List<PhotoDto> photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
