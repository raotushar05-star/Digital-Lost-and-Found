package com.lostandfound.repository;

import com.lostandfound.entity.HandoverRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HandoverRecordRepository extends JpaRepository<HandoverRecord, UUID> {
    Optional<HandoverRecord> findByFoundItem_FoundItemId(UUID foundItemId);
    Optional<HandoverRecord> findByClaim_ClaimId(UUID claimId);
    long count();
}
