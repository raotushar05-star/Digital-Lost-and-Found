package com.lostandfound.repository;

import com.lostandfound.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("select a from AuditLog a left join a.user u "
            + "where lower(a.action) like lower(concat('%', :action, '%')) "
            + "and (lower(coalesce(u.name, '')) like lower(concat('%', :q, '%')) "
            + "or lower(coalesce(u.email, '')) like lower(concat('%', :q, '%'))) "
            + "order by a.createdAt desc")
    Page<AuditLog> search(@Param("action") String action, @Param("q") String q, Pageable pageable);
}