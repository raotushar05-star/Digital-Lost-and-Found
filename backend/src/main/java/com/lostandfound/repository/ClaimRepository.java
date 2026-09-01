package com.lostandfound.repository;

import com.lostandfound.entity.Claim;
import com.lostandfound.entity.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    List<Claim> findByClaimant_UserIdOrderByCreatedAtDesc(UUID claimantId);
    List<Claim> findByFoundItem_FoundItemIdOrderByCreatedAtDesc(UUID foundItemId);
    long countByFoundItem_FoundItemId(UUID foundItemId);
    long countByStatus(ClaimStatus status);
    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);
    long countByFoundItem_Station_StationIdAndStatus(UUID stationId, ClaimStatus status);
}
