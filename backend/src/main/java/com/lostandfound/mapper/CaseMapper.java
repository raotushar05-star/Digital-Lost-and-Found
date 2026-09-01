package com.lostandfound.mapper;

import com.lostandfound.dto.casedto.CaseDto;
import com.lostandfound.entity.Case;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {
    public CaseDto toDto(Case c) {
        if (c == null) return null;
        return CaseDto.builder()
                .caseId(c.getCaseId())
                .caseNumber(c.getCaseNumber())
                .caseType(c.getCaseType().name())
                .currentStatus(c.getCurrentStatus().name())
                .lostItemId(c.getLostItem() != null ? c.getLostItem().getLostItemId() : null)
                .foundItemId(c.getFoundItem() != null ? c.getFoundItem().getFoundItemId() : null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
