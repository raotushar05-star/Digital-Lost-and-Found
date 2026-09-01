package com.lostandfound.repository;

import com.lostandfound.entity.CaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CaseStatusHistoryRepository extends JpaRepository<CaseStatusHistory, UUID> {
    List<CaseStatusHistory> findByCaseRef_CaseIdOrderByChangedAtAsc(UUID caseId);
}
