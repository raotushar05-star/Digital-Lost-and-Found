package com.lostandfound.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "custody_transfers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustodyTransfer {

    @Id
    @GeneratedValue
    @Column(name = "transfer_id")
    private UUID transferId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "found_item_id", nullable = false)
    private FoundItem foundItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_station_id", nullable = false)
    private PoliceStation fromStation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_station_id", nullable = false)
    private PoliceStation toStation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transferred_by", nullable = false)
    private User transferredBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "transferred_at", nullable = false)
    private LocalDateTime transferredAt;

    @PrePersist
    void onCreate() {
        transferredAt = LocalDateTime.now();
    }
}