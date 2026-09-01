package com.lostandfound.repository;

import com.lostandfound.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByLostItem_LostItemIdOrderByMatchScoreDesc(UUID lostItemId);
    List<Match> findByFoundItem_FoundItemIdOrderByMatchScoreDesc(UUID foundItemId);
    boolean existsByLostItem_LostItemIdAndFoundItem_FoundItemId(UUID lostItemId, UUID foundItemId);
    List<Match> findByLostItem_Owner_UserIdOrderByCreatedAtDesc(UUID ownerId);
}
