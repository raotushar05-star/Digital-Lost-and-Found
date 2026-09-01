package com.lostandfound.service;

import com.lostandfound.dto.file.PhotoUploadResponse;
import com.lostandfound.dto.founditem.*;
import com.lostandfound.dto.verification.VerificationResponse;
import com.lostandfound.dto.verification.VerifyFoundItemRequest;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.*;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.FoundItemMapper;
import com.lostandfound.repository.*;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Police Found-Item Intake + Verification. Implements the frozen custody rule:
 * only an authorized police officer creates/verifies the official found_item.
 * A found_report is optional context; direct station intake is equally valid.
 */
@Service
@RequiredArgsConstructor
public class FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final FoundReportRepository foundReportRepository;
    private final PoliceStationRepository stationRepository;
    private final CategoryRepository categoryRepository;
    private final ItemPhotoRepository itemPhotoRepository;
    private final CaseRepository caseRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final LocationService locationService;
    private final CaseService caseService;
    private final MatchingService matchingService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final FoundItemMapper foundItemMapper;

    @Transactional
    public FoundItemIntakeResponse intake(User officer, FoundItemIntakeRequest request) {
        PoliceStation station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Police station not found: " + request.getStationId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        Location location = locationService.createFromDto(request.getLocation());

        FoundReport linkedReport = null;
        if (request.getFoundReportId() != null) {
            linkedReport = foundReportRepository.findById(request.getFoundReportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Found report not found: " + request.getFoundReportId()));
            if (foundItemRepository.findByFoundReport_FoundReportId(linkedReport.getFoundReportId()).isPresent()) {
                throw new BadRequestException("This found report has already been linked to an official found item");
            }
        }

        FoundItem foundItem = FoundItem.builder()
                .foundReport(linkedReport)
                .station(station)
                .category(category)
                .description(request.getDescription())
                .brand(request.getBrand())
                .color(request.getColor())
                .privateIdentifyingDetails(request.getPrivateIdentifyingDetails())
                .foundDate(request.getFoundDate())
                .location(location)
                .custodyStatus(CustodyStatus.IN_CUSTODY)
                .verificationStatus(FoundItemVerificationStatus.PENDING)
                .build();
        foundItem = foundItemRepository.save(foundItem);

        if (linkedReport != null) {
            linkedReport.setStatus(FoundReportStatus.LINKED);
            foundReportRepository.save(linkedReport);
        }

        Case createdCase = caseService.createForFoundItem(foundItem, officer);
        auditService.log(officer, "FOUND_ITEM_REGISTERED", "FoundItem", foundItem.getFoundItemId());

        return FoundItemIntakeResponse.builder()
                .foundItemId(foundItem.getFoundItemId())
                .status(createdCase.getCurrentStatus().name())
                .message("Found item registered successfully")
                .build();
    }

    @Transactional
    public VerificationResponse verify(UUID foundItemId, User officer, VerifyFoundItemRequest request) {
        FoundItem foundItem = getEntityById(foundItemId);
        if (foundItem.getVerificationStatus() != FoundItemVerificationStatus.PENDING) {
            throw new BadRequestException("This found item has already been reviewed (" + foundItem.getVerificationStatus() + ")");
        }
        VerificationDecision decision;
        try {
            decision = VerificationDecision.valueOf(request.getDecision());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Decision must be VERIFIED or REJECTED");
        }

        VerificationRecord record = VerificationRecord.builder()
                .foundItem(foundItem)
                .officer(officer)
                .verificationType(VerificationType.FOUND_ITEM_VERIFICATION)
                .decision(decision)
                .verificationNotes(request.getVerificationNotes())
                .build();
        record = verificationRecordRepository.save(record);

        if (decision == VerificationDecision.VERIFIED) {
            foundItem.setVerificationStatus(FoundItemVerificationStatus.VERIFIED);
            foundItemRepository.save(foundItem);
            caseService.findAndTransition(foundItem, CaseStatus.POLICE_VERIFIED, officer,
                    "Found item verified by police: " + (request.getVerificationNotes() != null ? request.getVerificationNotes() : ""));
            matchingService.generateForFoundItem(foundItem);
        } else {
            foundItem.setVerificationStatus(FoundItemVerificationStatus.REJECTED);
            foundItemRepository.save(foundItem);
            caseService.findAndTransition(foundItem, CaseStatus.REJECTED, officer,
                    "Found item verification rejected: " + (request.getVerificationNotes() != null ? request.getVerificationNotes() : ""));
        }
        auditService.log(officer, "FOUND_ITEM_VERIFICATION_DECISION", "FoundItem", foundItem.getFoundItemId());

        return VerificationResponse.builder()
                .verificationId(record.getVerificationId())
                .foundItemId(foundItem.getFoundItemId())
                .decision(decision.name())
                .verifiedAt(record.getVerifiedAt())
                .build();
    }

    public FoundItemAdminDto getAdminDetail(UUID foundItemId, UserPrincipal principal) {
        assertPolice(principal);
        FoundItem item = getEntityById(foundItemId);
        Case linkedCase = caseRepository.findByFoundItem_FoundItemId(foundItemId).orElse(null);
        return foundItemMapper.toAdminDto(item, linkedCase);
    }

    public FoundItemPublicDto getPublicDetail(UUID foundItemId) {
        FoundItem item = getEntityById(foundItemId);
        if (item.getVerificationStatus() != FoundItemVerificationStatus.VERIFIED) {
            throw new ResourceNotFoundException("Found item not found: " + foundItemId);
        }
        return foundItemMapper.toPublicDto(item);
    }

    public List<FoundItemAdminDto> getStationInventory(UUID stationId, UserPrincipal principal) {
        assertPolice(principal);
        if ("POLICE_OFFICER".equals(principal.getRole()) && !stationId.equals(principal.getStationId())) {
            throw new ForbiddenException("Officers may only view their own station's inventory");
        }
        return foundItemRepository.findByStation_StationIdOrderByCreatedAtDesc(stationId).stream()
                .map(item -> foundItemMapper.toAdminDto(item, caseRepository.findByFoundItem_FoundItemId(item.getFoundItemId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @Transactional
    public PhotoUploadResponse uploadPhoto(UUID foundItemId, MultipartFile file, String visibility, User officer) {
        FoundItem item = getEntityById(foundItemId);
        PhotoVisibility photoVisibility;
        try {
            photoVisibility = visibility != null ? PhotoVisibility.valueOf(visibility) : PhotoVisibility.PUBLIC;
        } catch (IllegalArgumentException ex) {
            photoVisibility = PhotoVisibility.PUBLIC;
        }
        String url = fileStorageService.store(file, "found-items");
        boolean isFirst = item.getPhotos().isEmpty();
        ItemPhoto photo = ItemPhoto.builder()
                .foundItem(item)
                .fileUrl(url)
                .isPrimary(isFirst)
                .visibility(photoVisibility)
                .uploadedBy(officer)
                .build();
        photo = itemPhotoRepository.save(photo);
        return PhotoUploadResponse.builder()
                .photoId(photo.getPhotoId())
                .fileUrl(photo.getFileUrl())
                .message("Photo uploaded successfully")
                .build();
    }

    public FoundItem getEntityById(UUID foundItemId) {
        return foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found: " + foundItemId));
    }

    private void assertPolice(UserPrincipal principal) {
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (!isPolice) {
            throw new ForbiddenException("This action is restricted to police personnel");
        }
    }
}
