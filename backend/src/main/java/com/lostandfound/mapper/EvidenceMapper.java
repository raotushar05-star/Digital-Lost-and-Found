package com.lostandfound.mapper;

import com.lostandfound.dto.evidence.EvidenceDto;
import com.lostandfound.entity.ClaimEvidence;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EvidenceMapper {
    public EvidenceDto toDto(ClaimEvidence e) {
        if (e == null) return null;
        return EvidenceDto.builder()
                .evidenceId(e.getEvidenceId())
                .claimId(e.getClaim().getClaimId())
                .evidenceType(e.getEvidenceType().name())
                .description(e.getDescription())
                .fileUrl(e.getFileUrl())
                .verificationStatus(e.getVerificationStatus().name())
                .verifiedBy(e.getVerifiedBy() != null ? e.getVerifiedBy().getUserId() : null)
                .verifiedAt(e.getVerifiedAt())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public List<EvidenceDto> toDtoList(List<ClaimEvidence> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }
}
