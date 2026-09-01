package com.lostandfound.repository;

import com.lostandfound.entity.LostItem;
import com.lostandfound.entity.enums.LostItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LostItemRepository extends JpaRepository<LostItem, UUID> {
    List<LostItem> findByOwner_UserIdOrderByCreatedAtDesc(UUID ownerId);
    List<LostItem> findByStatus(LostItemStatus status);
    long countByStatus(LostItemStatus status);
}
