package com.lostandfound.service;

import com.lostandfound.dto.casedto.CaseDto;
import com.lostandfound.dto.casedto.CaseHistoryEntryDto;
import com.lostandfound.dto.casedto.CaseHistoryResponse;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.CaseStatus;
import com.lostandfound.entity.enums.CaseType;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.CaseMapper;
import com.lostandfound.repository.CaseRepository;
import com.lostandfound.repository.CaseStatusHistoryRepository;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.util.CaseNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * cases provides the unique case identity; case_status_history preserves the
 * complete progression of that case. This service owns both.
 */
@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseStatusHistoryRepository historyRepository;
    private final CaseNumberGenerator caseNumberGenerator;
    private final CaseMapper caseMapper;

    @Transactional
    public Case createForLostItem(LostItem lostItem, User createdBy) {
        Case c = Case.builder()
                .caseNumber(caseNumberGenerator.next())
                .lostItem(lostItem)
                .caseType(CaseType.LOST)
                .currentStatus(CaseStatus.REPORTED)
                .build();
        c = caseRepository.save(c);
        recordHistory(c, null, CaseStatus.REPORTED, createdBy, "Lost item reported");
        return c;
    }

    @Transactional
    public Case createForFoundItem(FoundItem foundItem, User createdBy) {
        Case c = Case.builder()
                .caseNumber(caseNumberGenerator.next())
                .foundItem(foundItem)
                .caseType(CaseType.FOUND)
                .currentStatus(CaseStatus.RECEIVED)
                .build();
        c = caseRepository.save(c);
        recordHistory(c, null, CaseStatus.RECEIVED, createdBy, "Found item received by police");
        return c;
    }

    @Transactional
    public void transition(Case c, CaseStatus newStatus, User changedBy, String remarks) {
        CaseStatus old = c.getCurrentStatus();
        c.setCurrentStatus(newStatus);
        caseRepository.save(c);
        recordHistory(c, old, newStatus, changedBy, remarks);
    }

    private void recordHistory(Case c, CaseStatus oldStatus, CaseStatus newStatus, User changedBy, String remarks) {
        CaseStatusHistory history = CaseStatusHistory.builder()
                .caseRef(c)
                .oldStatus(oldStatus != null ? oldStatus.name() : null)
                .newStatus(newStatus.name())
                .changedBy(changedBy)
                .remarks(remarks)
                .build();
        historyRepository.save(history);
    }

    /** Convenience: look up the case for a lost item and transition it, if one exists. */
    @Transactional
    public void findAndTransition(LostItem lostItem, CaseStatus newStatus, User changedBy, String remarks) {
        caseRepository.findByLostItem_LostItemId(lostItem.getLostItemId())
                .ifPresent(c -> transition(c, newStatus, changedBy, remarks));
    }

    /** Convenience: look up the case for a found item and transition it, if one exists. */
    @Transactional
    public void findAndTransition(FoundItem foundItem, CaseStatus newStatus, User changedBy, String remarks) {
        caseRepository.findByFoundItem_FoundItemId(foundItem.getFoundItemId())
                .ifPresent(c -> transition(c, newStatus, changedBy, remarks));
    }

    public Case getById(UUID caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));
    }

    public CaseDto getDto(UUID caseId, UserPrincipal principal) {
        Case c = getById(caseId);
        assertCanView(c, principal);
        return caseMapper.toDto(c);
    }

    public CaseHistoryResponse getHistory(UUID caseId, UserPrincipal principal) {
        Case c = getById(caseId);
        assertCanView(c, principal);
        List<CaseHistoryEntryDto> entries = historyRepository.findByCaseRef_CaseIdOrderByChangedAtAsc(caseId).stream()
                .map(h -> CaseHistoryEntryDto.builder()
                        .oldStatus(h.getOldStatus())
                        .newStatus(h.getNewStatus())
                        .remarks(h.getRemarks())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());
        return CaseHistoryResponse.builder().caseId(caseId).history(entries).build();
    }

    private void assertCanView(Case c, UserPrincipal principal) {
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (isPolice) return;
        boolean ownsLost = c.getLostItem() != null && c.getLostItem().getOwner().getUserId().equals(principal.getUserId());
        if (!ownsLost) {
            throw new ForbiddenException("You do not have permission to view this case");
        }
    }
}
