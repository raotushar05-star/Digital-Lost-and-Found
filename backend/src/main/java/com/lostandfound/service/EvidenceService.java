package com.lostandfound.service;

import com.lostandfound.dto.evidence.EvidenceCreateRequest;
import com.lostandfound.dto.evidence.EvidenceDto;
import com.lostandfound.entity.Claim;
import com.lostandfound.entity.ClaimEvidence;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.CaseStatus;
import com.lostandfound.entity.enums.ClaimStatus;
import com.lostandfound.entity.enums.EvidenceType;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.mapper.EvidenceMapper;
import com.lostandfound.repository.ClaimEvidenceRepository;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Claim Evidence Module. Evidence records are evaluated independently
 * (receipts, photographs, serial numbers, unique marks, etc.) - a claim
 * can carry multiple pieces of evidence.
 */
@Service
@RequiredArgsConstructor
public class EvidenceService {

    private final ClaimEvidenceRepository evidenceRepository;
    private final ClaimRepository claimRepository;
    private final CaseService caseService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final EvidenceMapper evidenceMapper;

    @Transactional
    public EvidenceDto addEvidence(UUID claimId, User claimant, EvidenceCreateRequest request, MultipartFile file) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new com.lostandfound.exception.ResourceNotFoundException("Claim not found: " + claimId));
        if (!claim.getClaimant().getUserId().equals(claimant.getUserId())) {
            throw new ForbiddenException("You can only add evidence to your own claim");
        }
        if (claim.getStatus() == ClaimStatus.APPROVED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new BadRequestException("Evidence cannot be added once a claim has been finalized");
        }

        EvidenceType evidenceType;
        try {
            evidenceType = EvidenceType.valueOf(request.getEvidenceType());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid evidence type: " + request.getEvidenceType());
        }

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = fileStorageService.store(file, "evidence");
        }

        ClaimEvidence evidence = ClaimEvidence.builder()
                .claim(claim)
                .evidenceType(evidenceType)
                .description(request.getDescription())
                .fileUrl(fileUrl)
                .build();
        evidence = evidenceRepository.save(evidence);

        if (claim.getStatus() == ClaimStatus.PENDING) {
            claim.setStatus(ClaimStatus.UNDER_VERIFICATION);
            claimRepository.save(claim);
            caseService.findAndTransition(claim.getFoundItem(), CaseStatus.UNDER_VERIFICATION, claimant, "Evidence submitted for review");
            if (claim.getLostItem() != null) {
                caseService.findAndTransition(claim.getLostItem(), CaseStatus.UNDER_VERIFICATION, claimant, "Evidence submitted for review");
            }
        }

        auditService.log(claimant, "EVIDENCE_SUBMITTED", "ClaimEvidence", evidence.getEvidenceId());
        return evidenceMapper.toDto(evidence);
    }

    public List<EvidenceDto> getEvidence(UUID claimId, UserPrincipal principal) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new com.lostandfound.exception.ResourceNotFoundException("Claim not found: " + claimId));
        boolean isClaimant = claim.getClaimant().getUserId().equals(principal.getUserId());
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (!isClaimant && !isPolice) {
            throw new ForbiddenException("You do not have permission to view this evidence");
        }
        return evidenceMapper.toDtoList(evidenceRepository.findByClaim_ClaimIdOrderByCreatedAtAsc(claimId));
    }
}
