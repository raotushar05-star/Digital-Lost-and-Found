package com.lostandfound.service;

import com.lostandfound.dto.casedto.CaseFileDto;
import com.lostandfound.dto.casedto.CaseFileDto.*;
import com.lostandfound.entity.*;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.*;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseFileService {

    private final CaseRepository caseRepository;
    private final CaseStatusHistoryRepository historyRepository;
    private final ClaimRepository claimRepository;
    private final ClaimDisputeRepository disputeRepository;
    private final HandoverRecordRepository handoverRecordRepository;
    private final MatchRepository matchRepository;
    private final ItemPhotoRepository itemPhotoRepository;

    @Transactional(readOnly = true)
    public List<CaseFileDto> listForPolice(UserPrincipal principal, LocalDate dateFrom, LocalDate dateTo) {
        assertPolice(principal);
        return caseRepository.findAll().stream()
                .filter(c -> inRange(c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate() : null, dateFrom, dateTo))
                .filter(c -> canAccessStation(c, principal))
                .sorted(Comparator.comparing(Case::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(c -> toDto(c, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CaseFileDto getById(UUID caseId, UserPrincipal principal) {
        Case c = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found: " + caseId));
        assertCanView(c, principal);
        boolean restricted = isPolice(principal);
        if (isPolice(principal) && !canAccessStation(c, principal) && !"SYSTEM_ADMIN".equals(principal.getRole())) {
            throw new ForbiddenException("This case is assigned to another police station");
        }
        return toDto(c, restricted);
    }

    private CaseFileDto toDto(Case c, boolean restricted) {
        LostItem lost = c.getLostItem();
        FoundItem found = c.getFoundItem();
        PoliceStation station = resolveStation(lost, found);
        User complainant = lost != null ? lost.getOwner()
                : (found != null && found.getFoundReport() != null ? found.getFoundReport().getFinder() : null);

        Location location = lost != null ? lost.getLocation() : (found != null ? found.getLocation() : null);
        List<ItemPhoto> photos = lost != null
                ? itemPhotoRepository.findByLostItem_LostItemId(lost.getLostItemId())
                : found != null ? itemPhotoRepository.findByFoundItem_FoundItemId(found.getFoundItemId()) : List.of();

        String identifying = lost != null ? lost.getIdentifyingDetails()
                : (found != null && restricted ? found.getPrivateIdentifyingDetails() : null);

        PropertyBlock property = PropertyBlock.builder()
                .itemId(lost != null ? lost.getLostItemId() : found != null ? found.getFoundItemId() : null)
                .category(lost != null ? lost.getCategory().getCategoryName()
                        : found != null ? found.getCategory().getCategoryName() : null)
                .description(lost != null ? lost.getDescription() : found != null ? found.getDescription() : null)
                .brand(lost != null ? lost.getBrand() : found != null ? found.getBrand() : null)
                .color(lost != null ? lost.getColor() : found != null ? found.getColor() : null)
                .identifyingDetails(identifying)
                .photoUrls(photos.stream().map(ItemPhoto::getFileUrl).toList())
                .itemStatus(lost != null ? lost.getStatus().name() : found != null ? found.getCustodyStatus().name() : null)
                .build();

        OccurrenceBlock occurrence = location == null ? null : OccurrenceBlock.builder()
                .occurrenceDate(lost != null ? lost.getLostDate() : found != null ? found.getFoundDate() : null)
                .addressText(location.getAddressText())
                .locality(location.getLocality())
                .city(location.getCity())
                .state(location.getState())
                .postalCode(location.getPostalCode())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();

        CustodyBlock custody = null;
        if (found != null) {
            User finder = found.getFoundReport() != null ? found.getFoundReport().getFinder() : null;
            custody = CustodyBlock.builder()
                    .custodyStatus(found.getCustodyStatus().name())
                    .verificationStatus(found.getVerificationStatus().name())
                    .receivedAt(found.getReceivedDate())
                    .finder(toPerson(finder, "Finder / informant"))
                    .foundReportStatus(found.getFoundReport() != null ? found.getFoundReport().getStatus().name() : null)
                    .build();
        }

        Map<UUID, Claim> claimsById = new LinkedHashMap<>();
        if (found != null) {
            claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(found.getFoundItemId())
                    .forEach(claim -> claimsById.put(claim.getClaimId(), claim));
        }
        if (lost != null) {
            claimRepository.findByLostItem_LostItemIdOrderByCreatedAtDesc(lost.getLostItemId())
                    .forEach(claim -> claimsById.putIfAbsent(claim.getClaimId(), claim));
        }
        List<Claim> claims = new ArrayList<>(claimsById.values());

        List<Match> matches = new ArrayList<>();
        if (lost != null) {
            matches.addAll(matchRepository.findByLostItem_LostItemIdOrderByMatchScoreDesc(lost.getLostItemId()));
        }
        if (found != null) {
            matches.addAll(matchRepository.findByFoundItem_FoundItemIdOrderByMatchScoreDesc(found.getFoundItemId()));
        }

        HandoverRecord handover = null;
        if (found != null) {
            handover = handoverRecordRepository.findByFoundItem_FoundItemId(found.getFoundItemId()).orElse(null);
        }

        List<ClaimDispute> disputes = found != null
                ? disputeRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(found.getFoundItemId())
                : List.of();

        List<CaseStatusHistory> history = historyRepository.findByCaseRef_CaseIdOrderByChangedAtAsc(c.getCaseId());

        return CaseFileDto.builder()
                .caseId(c.getCaseId())
                .caseNumber(c.getCaseNumber())
                .caseType(c.getCaseType().name())
                .currentStatus(c.getCurrentStatus().name())
                .registeredAt(c.getCreatedAt())
                .lastUpdatedAt(c.getUpdatedAt())
                .station(toStation(station))
                .complainant(toPerson(complainant, lost != null ? "Complainant (owner)" : "Informant"))
                .occurrence(occurrence)
                .property(property)
                .custody(custody)
                .matches(matches.stream().map(m -> toMatch(m, lost != null)).toList())
                .claims(claims.stream().map(this::toClaim).toList())
                .disputes(disputes.stream().map(this::toDispute).toList())
                .caseDiary(history.stream().map(this::toHistory).toList())
                .handover(toHandover(handover))
                .build();
    }

    private MatchBlock toMatch(Match m, boolean fromLost) {
        FoundItem found = m.getFoundItem();
        LostItem lost = m.getLostItem();
        return MatchBlock.builder()
                .matchId(m.getMatchId())
                .counterpartType(fromLost ? "FOUND" : "LOST")
                .counterpartItemId(fromLost ? found.getFoundItemId() : lost.getLostItemId())
                .counterpartCategory(fromLost ? found.getCategory().getCategoryName() : lost.getCategory().getCategoryName())
                .counterpartDescription(fromLost ? found.getDescription() : lost.getDescription())
                .matchScore(m.getMatchScore())
                .matchReason(m.getMatchReason())
                .status(m.getStatus().name())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private ClaimBlock toClaim(Claim claim) {
        User claimant = claim.getClaimant();
        return ClaimBlock.builder()
                .claimId(claim.getClaimId())
                .claimantName(claimant != null ? claimant.getName() : null)
                .claimantEmail(claimant != null ? claimant.getEmail() : null)
                .claimantPhone(claimant != null ? claimant.getPhone() : null)
                .claimDetails(claim.getClaimDetails())
                .status(claim.getStatus().name())
                .reviewedByName(claim.getReviewedBy() != null ? claim.getReviewedBy().getName() : null)
                .reviewedAt(claim.getReviewedAt())
                .filedAt(claim.getCreatedAt())
                .evidence(claim.getEvidenceList() == null ? List.of() : claim.getEvidenceList().stream()
                        .map(e -> EvidenceBlock.builder()
                                .evidenceType(e.getEvidenceType() != null ? e.getEvidenceType().name() : null)
                                .description(e.getDescription())
                                .verificationStatus(e.getVerificationStatus() != null ? e.getVerificationStatus().name() : null)
                                .fileUrl(e.getFileUrl())
                                .build())
                        .toList())
                .build();
    }

    private DisputeBlock toDispute(ClaimDispute d) {
        return DisputeBlock.builder()
                .disputeId(d.getDisputeId())
                .raisedByName(d.getRaisedBy() != null ? d.getRaisedBy().getName() : null)
                .reason(d.getReason())
                .status(d.getStatus().name())
                .resolution(d.getResolution())
                .resolvedByName(d.getResolvedBy() != null ? d.getResolvedBy().getName() : null)
                .resolvedAt(d.getResolvedAt())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private HistoryBlock toHistory(CaseStatusHistory h) {
        User by = h.getChangedBy();
        return HistoryBlock.builder()
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .remarks(h.getRemarks())
                .recordedByName(by != null ? by.getName() : null)
                .recordedByRole(by != null && by.getRole() != null ? by.getRole().name() : null)
                .changedAt(h.getChangedAt())
                .build();
    }

    private HandoverBlock toHandover(HandoverRecord h) {
        if (h == null) return null;
        return HandoverBlock.builder()
                .handoverId(h.getHandoverId())
                .handoverDate(h.getHandoverDate())
                .recipientName(h.getRecipient() != null ? h.getRecipient().getName() : null)
                .recipientPhone(h.getRecipient() != null ? h.getRecipient().getPhone() : null)
                .officerName(h.getOfficer() != null ? h.getOfficer().getName() : null)
                .notes(h.getHandoverNotes())
                .acknowledgementReference(h.getAcknowledgementReference())
                .build();
    }

    private PersonBlock toPerson(User user, String roleLabel) {
        if (user == null) return null;
        return PersonBlock.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roleLabel(roleLabel)
                .build();
    }

    private StationBlock toStation(PoliceStation station) {
        if (station == null) return null;
        return StationBlock.builder()
                .stationId(station.getStationId())
                .stationName(station.getStationName())
                .stationCode(station.getStationCode())
                .address(station.getAddress())
                .phone(station.getPhone())
                .city(station.getLocation() != null ? station.getLocation().getCity() : null)
                .build();
    }

    private PoliceStation resolveStation(LostItem lost, FoundItem found) {
        if (found != null && found.getStation() != null) return found.getStation();
        if (lost != null) return lost.getAssignedStation();
        return null;
    }

    private boolean canAccessStation(Case c, UserPrincipal principal) {
        if ("SYSTEM_ADMIN".equals(principal.getRole())) return true;
        UUID stationId = principal.getStationId();
        if (stationId == null) return false;
        FoundItem found = c.getFoundItem();
        if (found != null && found.getStation() != null) {
            return stationId.equals(found.getStation().getStationId());
        }
        LostItem lost = c.getLostItem();
        if (lost == null) return false;
        if (lost.getAssignedStation() == null) return true;
        return stationId.equals(lost.getAssignedStation().getStationId());
    }

    private void assertCanView(Case c, UserPrincipal principal) {
        if (isPolice(principal) || "SYSTEM_ADMIN".equals(principal.getRole())) return;
        boolean ownsLost = c.getLostItem() != null
                && c.getLostItem().getOwner().getUserId().equals(principal.getUserId());
        boolean isFinder = c.getFoundItem() != null
                && c.getFoundItem().getFoundReport() != null
                && c.getFoundItem().getFoundReport().getFinder().getUserId().equals(principal.getUserId());
        if (!ownsLost && !isFinder) {
            throw new ForbiddenException("You do not have permission to view this case");
        }
    }

    private void assertPolice(UserPrincipal principal) {
        if (!isPolice(principal) && !"SYSTEM_ADMIN".equals(principal.getRole())) {
            throw new ForbiddenException("Only police personnel can export station case files");
        }
    }

    private boolean isPolice(UserPrincipal principal) {
        return "POLICE_OFFICER".equals(principal.getRole())
                || "POLICE_ADMIN".equals(principal.getRole())
                || "SYSTEM_ADMIN".equals(principal.getRole());
    }

    private boolean inRange(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null) return true;
        if (from != null && date.isBefore(from)) return false;
        if (to != null && date.isAfter(to)) return false;
        return true;
    }
}
