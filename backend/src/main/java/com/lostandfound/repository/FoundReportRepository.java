package com.lostandfound.repository;

import com.lostandfound.entity.FoundReport;
import com.lostandfound.entity.enums.FoundReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FoundReportRepository extends JpaRepository<FoundReport, UUID> {
    List<FoundReport> findByFinder_UserIdOrderByCreatedAtDesc(UUID finderId);
    long countByStatus(FoundReportStatus status);
}
