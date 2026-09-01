package com.lostandfound.entity;

import com.lostandfound.entity.enums.CaseStatus;
import com.lostandfound.entity.enums.CaseType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Case {

    @Id
    @GeneratedValue
    @Column(name = "case_id")
    private UUID caseId;

    @Column(name = "case_number", nullable = false, unique = true, length = 30)
    private String caseNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", unique = true)
    private LostItem lostItem;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id", unique = true)
    private FoundItem foundItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false, length = 20)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 40)
    @Builder.Default
    private CaseStatus currentStatus = CaseStatus.REPORTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
