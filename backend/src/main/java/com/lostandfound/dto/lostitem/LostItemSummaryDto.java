package com.lostandfound.dto.lostitem;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LostItemSummaryDto {
    private UUID lostItemId;
    private String category;
    private String description;
    private String color;
    private String brand;
    private LocalDate lostDate;
    private String city;
    private String status;
    private UUID caseId;
    private String caseNumber;
    private LocalDateTime createdAt;
}
