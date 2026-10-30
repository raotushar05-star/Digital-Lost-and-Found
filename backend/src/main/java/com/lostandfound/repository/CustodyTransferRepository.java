package com.lostandfound.repository;

import com.lostandfound.entity.CustodyTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustodyTransferRepository extends JpaRepository<CustodyTransfer, UUID> {
    List<CustodyTransfer> findByFoundItem_FoundItemIdOrderByTransferredAtDesc(UUID foundItemId);
}