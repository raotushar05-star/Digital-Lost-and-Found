package com.lostandfound.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "police_stations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PoliceStation {

    @Id
    @GeneratedValue
    @Column(name = "station_id")
    private UUID stationId;

    @Column(name = "station_name", nullable = false, length = 150)
    private String stationName;

    @Column(name = "station_code", nullable = false, unique = true, length = 30)
    private String stationCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(length = 15)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
