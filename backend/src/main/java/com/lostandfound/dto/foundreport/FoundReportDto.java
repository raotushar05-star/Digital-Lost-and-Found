package com.lostandfound.dto.foundreport;

import com.lostandfound.dto.location.LocationDto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoundReportDto {
    private UUID foundReportId;
    private UUID finderId;
    private String category;
    private String description;
    private String brand;
    private String color;
    private LocalDate foundDate;
    private LocationDto location;
    private String status;
    private UUID linkedFoundItemId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
