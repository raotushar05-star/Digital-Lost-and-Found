package com.lostandfound.repository;

import com.lostandfound.entity.VerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, UUID> {
    List<VerificationRecord> findByFoundItem_FoundItemIdOrderByVerifiedAtDesc(UUID foundItemId);
    List<VerificationRecord> findByClaim_ClaimIdOrderByVerifiedAtDesc(UUID claimId);
}
