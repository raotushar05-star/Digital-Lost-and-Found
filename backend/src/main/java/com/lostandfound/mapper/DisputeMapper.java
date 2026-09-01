package com.lostandfound.mapper;

import com.lostandfound.dto.dispute.DisputeDto;
import com.lostandfound.entity.ClaimDispute;
import org.springframework.stereotype.Component;

@Component
public class DisputeMapper {
    public DisputeDto toDto(ClaimDispute d) {
        if (d == null) return null;
        return DisputeDto.builder()
                .disputeId(d.getDisputeId())
                .foundItemId(d.getFoundItem().getFoundItemId())
                .claimId(d.getClaim().getClaimId())
                .raisedBy(d.getRaisedBy() != null ? d.getRaisedBy().getUserId() : null)
                .raisedByName(d.getRaisedBy() != null ? d.getRaisedBy().getName() : null)
                .reason(d.getReason())
                .status(d.getStatus().name())
                .resolution(d.getResolution())
                .resolvedBy(d.getResolvedBy() != null ? d.getResolvedBy().getUserId() : null)
                .resolvedByName(d.getResolvedBy() != null ? d.getResolvedBy().getName() : null)
                .resolvedAt(d.getResolvedAt())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
