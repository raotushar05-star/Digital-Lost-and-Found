package com.lostandfound.repository;

import com.lostandfound.entity.LostItem;
import com.lostandfound.entity.enums.LostItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LostItemRepository extends JpaRepository<LostItem, UUID> {
    List<LostItem> findByOwner_UserIdOrderByCreatedAtDesc(UUID ownerId);
    List<LostItem> findByStatus(LostItemStatus status);
    List<LostItem> findAllByOrderByCreatedAtDesc();
    List<LostItem> findByStatusOrderByCreatedAtDesc(LostItemStatus status);
    List<LostItem> findByAssignedStation_StationIdOrderByCreatedAtDesc(UUID stationId);
    List<LostItem> findByAssignedStation_StationIdAndStatusOrderByCreatedAtDesc(UUID stationId, LostItemStatus status);
    List<LostItem> findByAssignedStationIsNullOrderByCreatedAtDesc();
    List<LostItem> findByAssignedStationIsNullAndStatusOrderByCreatedAtDesc(LostItemStatus status);
    long countByStatus(LostItemStatus status);
    long countByStatusAndAssignedStation_StationId(LostItemStatus status, UUID stationId);
    long countByStatusAndAssignedStationIsNull(LostItemStatus status);
}
