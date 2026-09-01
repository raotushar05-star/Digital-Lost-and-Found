package com.lostandfound.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "handover_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HandoverRecord {

    @Id
    @GeneratedValue
    @Column(name = "handover_id")
    private UUID handoverId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "found_item_id", nullable = false, unique = true)
    private FoundItem foundItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Column(name = "handover_date", nullable = false)
    private LocalDateTime handoverDate;

    @Column(name = "handover_notes", columnDefinition = "TEXT")
    private String handoverNotes;

    @Column(name = "acknowledgement_reference", length = 255)
    private String acknowledgementReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (handoverDate == null) {
            handoverDate = LocalDateTime.now();
        }
    }
}
