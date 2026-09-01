package com.lostandfound.mapper;

import com.lostandfound.dto.claim.ClaimDetailDto;
import com.lostandfound.dto.claim.ClaimSummaryDto;
import com.lostandfound.entity.Claim;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClaimMapper {

    private final EvidenceMapper evidenceMapper;

    public ClaimSummaryDto toSummaryDto(Claim claim) {
        if (claim == null) return null;
        return ClaimSummaryDto.builder()
                .claimId(claim.getClaimId())
                .foundItemId(claim.getFoundItem().getFoundItemId())
                .foundItemCategory(claim.getFoundItem().getCategory().getCategoryName())
                .foundItemDescription(claim.getFoundItem().getDescription())
                .claimantId(claim.getClaimant().getUserId())
                .claimantName(claim.getClaimant().getName())
                .status(claim.getStatus().name())
                .createdAt(claim.getCreatedAt())
                .build();
    }

    public ClaimDetailDto toDetailDto(Claim claim) {
        if (claim == null) return null;
        return ClaimDetailDto.builder()
                .claimId(claim.getClaimId())
                .foundItemId(claim.getFoundItem().getFoundItemId())
                .foundItemCategory(claim.getFoundItem().getCategory().getCategoryName())
                .foundItemDescription(claim.getFoundItem().getDescription())
                .claimantId(claim.getClaimant().getUserId())
                .claimantName(claim.getClaimant().getName())
                .lostItemId(claim.getLostItem() != null ? claim.getLostItem().getLostItemId() : null)
                .claimDetails(claim.getClaimDetails())
                .status(claim.getStatus().name())
                .reviewedBy(claim.getReviewedBy() != null ? claim.getReviewedBy().getUserId() : null)
                .reviewedByName(claim.getReviewedBy() != null ? claim.getReviewedBy().getName() : null)
                .reviewedAt(claim.getReviewedAt())
                .evidence(evidenceMapper.toDtoList(claim.getEvidenceList()))
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
