package com.lostandfound.repository;

import com.lostandfound.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUser_UserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Notification> findByUser_UserIdAndIsReadFalse(UUID userId);
    long countByUser_UserIdAndIsReadFalse(UUID userId);
}
