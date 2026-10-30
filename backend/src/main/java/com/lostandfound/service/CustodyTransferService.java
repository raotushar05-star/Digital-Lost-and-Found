package com.lostandfound.service;

import com.lostandfound.dto.custody.TransferDto;
import com.lostandfound.dto.custody.TransferRequest;
import com.lostandfound.entity.Case;
import com.lostandfound.entity.CustodyTransfer;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.ClaimStatus;
import com.lostandfound.entity.enums.CustodyStatus;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.*;
import com.lostandfound.security.StationAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Moves an item in custody from one station to another, with a logged reason. */
@Service
@RequiredArgsConstructor
public class CustodyTransferService {

    private final FoundItemRepository foundItemRepository;
    private final PoliceStationRepository stationRepository;
    private final CustodyTransferRepository transferRepository;
    private final ClaimRepository claimRepository;
    private final CaseRepository caseRepository;
    private final CaseService caseService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public TransferDto transfer(UUID foundItemId, User officer, TransferRequest request) {
        FoundItem item = load(foundItemId);
        StationAccess.assertOwnStation(officer, item.getStation());
        if (item.getCustodyStatus() != CustodyStatus.IN_CUSTODY) {
            throw new BadRequestException("Only items that are in custody can be transferred");
        }
        PoliceStation to = stationRepository.findById(request.getToStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Police station not found: " + request.getToStationId()));
        if (!Boolean.TRUE.equals(to.getIsActive())) {
            throw new BadRequestException("The destination station is inactive");
        }
        PoliceStation from = item.getStation();
        if (to.getStationId().equals(from.getStationId())) {
            throw new BadRequestException("The item is already at this station");
        }
        boolean awaitingHandover = claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItemId).stream()
                .anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED);
        if (awaitingHandover) {
            throw new BadRequestException("This item has an approved claim waiting for handover and cannot be transferred");
        }

        item.setStation(to);
        foundItemRepository.save(item);
        CustodyTransfer saved = transferRepository.save(CustodyTransfer.builder()
                .foundItem(item)
                .fromStation(from)
                .toStation(to)
                .transferredBy(officer)
                .reason(request.getReason().trim())
                .build());

        Case linkedCase = caseRepository.findByFoundItem_FoundItemId(foundItemId).orElse(null);
        String summary = "Transferred from " + from.getStationName() + " to " + to.getStationName()
                + ": " + request.getReason().trim();
        if (linkedCase != null) {
            caseService.transition(linkedCase, linkedCase.getCurrentStatus(), officer, summary);
        }
        List<User> staff = userRepository.findByStation_StationIdAndRoleIn(
                to.getStationId(), List.of(Role.POLICE_OFFICER, Role.POLICE_ADMIN));
        for (User user : staff) {
            if (Boolean.TRUE.equals(user.getIsActive())) {
                notificationService.notify(user, NotificationType.GENERAL, "Item transferred to your station",
                        item.getCategory().getCategoryName() + " transferred from " + from.getStationName()
                                + ". Reason: " + request.getReason().trim(),
                        linkedCase, null);
            }
        }
        auditService.log(officer, "CUSTODY_TRANSFERRED", "FoundItem", foundItemId);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TransferDto> history(UUID foundItemId, User officer) {
        FoundItem item = load(foundItemId);
        StationAccess.assertOwnStation(officer, item.getStation());
        return transferRepository.findByFoundItem_FoundItemIdOrderByTransferredAtDesc(foundItemId).stream()
                .map(this::toDto)
                .toList();
    }

    private FoundItem load(UUID foundItemId) {
        return foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found: " + foundItemId));
    }

    private TransferDto toDto(CustodyTransfer t) {
        return TransferDto.builder()
                .transferId(t.getTransferId())
                .fromStationName(t.getFromStation().getStationName())
                .toStationName(t.getToStation().getStationName())
                .transferredByName(t.getTransferredBy().getName())
                .reason(t.getReason())
                .transferredAt(t.getTransferredAt())
                .build();
    }
}