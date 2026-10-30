package com.lostandfound.dto.custody;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferDto {
    private UUID transferId;
    private String fromStationName;
    private String toStationName;
    private String transferredByName;
    private String reason;
    private LocalDateTime transferredAt;
}