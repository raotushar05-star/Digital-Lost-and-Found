package com.lostandfound.entity;

import com.lostandfound.entity.enums.CustodyStatus;
import com.lostandfound.entity.enums.FoundItemVerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "found_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoundItem {

    @Id
    @GeneratedValue
    @Column(name = "found_item_id")
    private UUID foundItemId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_report_id", unique = true)
    private FoundReport foundReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private PoliceStation station;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String brand;

    @Column(length = 50)
    private String color;

    @Column(name = "private_identifying_details", columnDefinition = "TEXT")
    private String privateIdentifyingDetails;

    @Column(name = "found_date", nullable = false)
    private LocalDate foundDate;

    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "custody_status", nullable = false, length = 30)
    @Builder.Default
    private CustodyStatus custodyStatus = CustodyStatus.IN_CUSTODY;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    @Builder.Default
    private FoundItemVerificationStatus verificationStatus = FoundItemVerificationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "foundItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemPhoto> photos = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (receivedDate == null) {
            receivedDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
