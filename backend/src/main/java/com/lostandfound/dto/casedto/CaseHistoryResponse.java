package com.lostandfound.dto.casedto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CaseHistoryResponse {
    private UUID caseId;
    private List<CaseHistoryEntryDto> history;
}
