package com.lostandfound.repository;

import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.enums.FoundItemVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoundItemRepository extends JpaRepository<FoundItem, UUID>, JpaSpecificationExecutor<FoundItem> {
    List<FoundItem> findByStation_StationIdOrderByCreatedAtDesc(UUID stationId);
    List<FoundItem> findByVerificationStatus(FoundItemVerificationStatus status);
    Optional<FoundItem> findByFoundReport_FoundReportId(UUID foundReportId);
    long countByVerificationStatus(FoundItemVerificationStatus status);
    long countByStation_StationIdAndVerificationStatus(UUID stationId, FoundItemVerificationStatus status);
    List<FoundItem> findByVerificationStatusOrderByCreatedAtDesc(FoundItemVerificationStatus status);
}
