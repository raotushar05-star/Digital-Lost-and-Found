package com.lostandfound.dto.match;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchDto {
    private UUID matchId;
    private UUID lostItemId;
    private UUID foundItemId;
    private String foundItemCategory;
    private String foundItemDescription;
    private LocalDate foundItemDate;
    private String foundItemCity;
    private BigDecimal matchScore;
    private String matchReason;
    private String status;
    private LocalDateTime createdAt;
}
