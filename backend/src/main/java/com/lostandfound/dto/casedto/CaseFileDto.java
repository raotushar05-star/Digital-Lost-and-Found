package com.lostandfound.dto.casedto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Police case diary / property-file snapshot used for official PDF export.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CaseFileDto {

    private UUID caseId;
    private String caseNumber;
    private String caseType;
    private String currentStatus;
    private LocalDateTime registeredAt;
    private LocalDateTime lastUpdatedAt;

    private StationBlock station;
    private PersonBlock complainant;
    private OccurrenceBlock occurrence;
    private PropertyBlock property;
    private CustodyBlock custody;

    @Builder.Default
    private List<MatchBlock> matches = new ArrayList<>();
    @Builder.Default
    private List<ClaimBlock> claims = new ArrayList<>();
    @Builder.Default
    private List<DisputeBlock> disputes = new ArrayList<>();
    @Builder.Default
    private List<HistoryBlock> caseDiary = new ArrayList<>();
    private HandoverBlock handover;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StationBlock {
        private UUID stationId;
        private String stationName;
        private String stationCode;
        private String address;
        private String phone;
        private String city;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PersonBlock {
        private String name;
        private String email;
        private String phone;
        private String address;
        private String roleLabel;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OccurrenceBlock {
        private LocalDate occurrenceDate;
        private String addressText;
        private String locality;
        private String city;
        private String state;
        private String postalCode;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PropertyBlock {
        private UUID itemId;
        private String category;
        private String description;
        private String brand;
        private String color;
        private String identifyingDetails;
        private List<String> photoUrls;
        private String itemStatus;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CustodyBlock {
        private String custodyStatus;
        private String verificationStatus;
        private LocalDateTime receivedAt;
        private PersonBlock finder;
        private String foundReportStatus;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MatchBlock {
        private UUID matchId;
        private String counterpartType;
        private UUID counterpartItemId;
        private String counterpartCategory;
        private String counterpartDescription;
        private BigDecimal matchScore;
        private String matchReason;
        private String status;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ClaimBlock {
        private UUID claimId;
        private String claimantName;
        private String claimantEmail;
        private String claimantPhone;
        private String claimDetails;
        private String status;
        private String reviewedByName;
        private LocalDateTime reviewedAt;
        private LocalDateTime filedAt;
        private List<EvidenceBlock> evidence;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EvidenceBlock {
        private String evidenceType;
        private String description;
        private String verificationStatus;
        private String fileUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DisputeBlock {
        private UUID disputeId;
        private String raisedByName;
        private String reason;
        private String status;
        private String resolution;
        private String resolvedByName;
        private LocalDateTime resolvedAt;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HistoryBlock {
        private String oldStatus;
        private String newStatus;
        private String remarks;
        private String recordedByName;
        private String recordedByRole;
        private LocalDateTime changedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HandoverBlock {
        private UUID handoverId;
        private LocalDateTime handoverDate;
        private String recipientName;
        private String recipientPhone;
        private String officerName;
        private String notes;
        private String acknowledgementReference;
    }
}
