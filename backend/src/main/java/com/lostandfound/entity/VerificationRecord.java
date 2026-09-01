package com.lostandfound.entity;

import com.lostandfound.entity.enums.VerificationDecision;
import com.lostandfound.entity.enums.VerificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationRecord {

    @Id
    @GeneratedValue
    @Column(name = "verification_id")
    private UUID verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id")
    private FoundItem foundItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false, length = 40)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationDecision decision;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    @PrePersist
    void onCreate() {
        if (verifiedAt == null) {
            verifiedAt = LocalDateTime.now();
        }
    }
}
