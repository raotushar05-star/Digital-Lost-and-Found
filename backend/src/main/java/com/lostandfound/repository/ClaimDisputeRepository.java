package com.lostandfound.repository;

import com.lostandfound.entity.ClaimDispute;
import com.lostandfound.entity.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimDisputeRepository extends JpaRepository<ClaimDispute, UUID> {
    List<ClaimDispute> findByFoundItem_FoundItemIdOrderByCreatedAtDesc(UUID foundItemId);
    List<ClaimDispute> findByStatusOrderByCreatedAtDesc(DisputeStatus status);
    long countByStatus(DisputeStatus status);
}
