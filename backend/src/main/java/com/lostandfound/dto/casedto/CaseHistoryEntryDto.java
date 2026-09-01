package com.lostandfound.dto.casedto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CaseHistoryEntryDto {
    private String oldStatus;
    private String newStatus;
    private String remarks;
    private LocalDateTime changedAt;
}
