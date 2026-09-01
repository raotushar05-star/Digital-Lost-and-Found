package com.lostandfound.repository;

import com.lostandfound.entity.Case;
import com.lostandfound.entity.enums.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<Case, UUID> {
    Optional<Case> findByLostItem_LostItemId(UUID lostItemId);
    Optional<Case> findByFoundItem_FoundItemId(UUID foundItemId);
    Optional<Case> findByCaseNumber(String caseNumber);
    long countByCurrentStatus(CaseStatus status);
    List<Case> findByCurrentStatus(CaseStatus status);
    long count();
}
