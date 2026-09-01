package com.lostandfound.repository;

import com.lostandfound.entity.ItemPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemPhotoRepository extends JpaRepository<ItemPhoto, UUID> {
    List<ItemPhoto> findByLostItem_LostItemId(UUID lostItemId);
    List<ItemPhoto> findByFoundItem_FoundItemId(UUID foundItemId);
}
