package com.lostandfound.service;

import com.lostandfound.dto.file.PhotoUploadResponse;
import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.dto.lostitem.*;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.LostItemStatus;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.entity.enums.PhotoVisibility;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.LostItemMapper;
import com.lostandfound.repository.CaseRepository;
import com.lostandfound.repository.CategoryRepository;
import com.lostandfound.repository.ItemPhotoRepository;
import com.lostandfound.repository.LostItemRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LostItemService {

    private static final Set<LostItemStatus> EDITABLE_STATUSES = Set.of(
            LostItemStatus.REPORTED, LostItemStatus.RECEIVED, LostItemStatus.POTENTIAL_MATCH);
        private static final Set<LostItemStatus> WITHDRAWABLE_STATUSES = Set.of(
            LostItemStatus.REPORTED, LostItemStatus.POTENTIAL_MATCH);

    private final LostItemRepository lostItemRepository;
    private final CategoryRepository categoryRepository;
    private final CaseRepository caseRepository;
    private final ItemPhotoRepository itemPhotoRepository;
    private final LocationService locationService;
    private final CaseService caseService;
    private final MatchingService matchingService;
    private final StationAssignmentService stationAssignmentService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final LostItemMapper lostItemMapper;

    @Transactional
    public LostItemCreateResponse create(UUID ownerId, LostItemCreateRequest request, User owner) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        Location location = locationService.createFromDto(request.getLocation());

        LostItem lostItem = LostItem.builder()
                .owner(owner)
                .category(category)
                .description(request.getDescription())
                .brand(request.getBrand())
                .color(request.getColor())
                .identifyingDetails(request.getIdentifyingDetails())
                .lostDate(request.getLostDate())
                .location(location)
                .assignedStation(stationAssignmentService.findNearestStation(location).orElse(null))
                .status(LostItemStatus.REPORTED)
                .build();
        lostItem = lostItemRepository.save(lostItem);

        Case linkedCase = caseService.createForLostItem(lostItem, owner);
        auditService.log(owner, "LOST_ITEM_REPORTED", "LostItem", lostItem.getLostItemId());
        matchingService.generateForLostItem(lostItem);
        notifyPoliceOfLostReport(lostItem, linkedCase);

        return LostItemCreateResponse.builder()
                .lostItemId(lostItem.getLostItemId())
                .status(lostItem.getStatus().name())
                .message("Lost item reported successfully")
                .build();
    }

            private void notifyPoliceOfLostReport(LostItem report, Case linkedCase) {
            List<User> recipients = new ArrayList<>();
            if (report.getAssignedStation() != null) {
                recipients.addAll(userRepository.findByStation_StationIdAndRoleIn(
                    report.getAssignedStation().getStationId(), List.of(Role.POLICE_OFFICER, Role.POLICE_ADMIN)));
            } else {
                recipients.addAll(userRepository.findByRole(Role.POLICE_OFFICER));
                recipients.addAll(userRepository.findByRole(Role.POLICE_ADMIN));
            }

            String assignment = report.getAssignedStation() == null
                ? "It could not be assigned to a station and is in the unassigned reports queue."
                : "It is assigned to " + report.getAssignedStation().getStationName() + ".";
            String message = "New " + report.getCategory().getCategoryName().toLowerCase()
                + " lost-item report " + (linkedCase != null ? linkedCase.getCaseNumber() : "")
                + " from " + report.getLocation().getCity() + ". " + assignment
                + " Open Lost Reports to review it.";

            recipients.stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .forEach(user -> notificationService.notify(
                    user,
                    NotificationType.GENERAL,
                    "New lost-item report assigned",
                    message,
                    linkedCase,
                    null
                ));
            }

    @Transactional(readOnly = true)
    public LostItemDetailDto getDetail(UUID lostItemId, UserPrincipal principal) {
        LostItem item = getEntityById(lostItemId);
        assertCanView(item, principal);
        Case linkedCase = caseRepository.findByLostItem_LostItemId(lostItemId).orElse(null);
        return lostItemMapper.toDetailDto(item, linkedCase);
    }

    @Transactional
    public LostItemDetailDto update(UUID lostItemId, LostItemUpdateRequest request, UserPrincipal principal) {
        LostItem item = getEntityById(lostItemId);
        assertOwner(item, principal);
        if (!EDITABLE_STATUSES.contains(item.getStatus())) {
            throw new BadRequestException("This lost-item report can no longer be edited in its current status: " + item.getStatus());
        }
        item.setDescription(request.getDescription());
        item.setBrand(request.getBrand());
        item.setColor(request.getColor());
        item.setIdentifyingDetails(request.getIdentifyingDetails());
        item.setLostDate(request.getLostDate());
        if (request.getLocation() != null) {
            LocationDto loc = request.getLocation();
            if (loc.getLocationId() != null) {
                item.setLocation(locationService.getById(loc.getLocationId()));
            } else {
                item.setLocation(locationService.createFromDto(loc));
            }
        }
        item = lostItemRepository.save(item);
        auditService.log(item.getOwner(), "LOST_ITEM_UPDATED", "LostItem", item.getLostItemId());
        Case linkedCase = caseRepository.findByLostItem_LostItemId(lostItemId).orElse(null);
        return lostItemMapper.toDetailDto(item, linkedCase);
    }

    @Transactional
    public void withdraw(UUID lostItemId, UserPrincipal principal) {
        LostItem item = getEntityById(lostItemId);
        assertOwner(item, principal);
        if (!WITHDRAWABLE_STATUSES.contains(item.getStatus())) {
            throw new BadRequestException("A lost-item report can only be withdrawn before police receive it");
        }
        item.setStatus(LostItemStatus.WITHDRAWN);
        lostItemRepository.save(item);
        auditService.log(item.getOwner(), "LOST_ITEM_WITHDRAWN", "LostItem", item.getLostItemId());
    }

    @Transactional
    public PhotoUploadResponse uploadPhoto(UUID lostItemId, MultipartFile file, UserPrincipal principal) {
        LostItem item = getEntityById(lostItemId);
        assertOwner(item, principal);
        String url = fileStorageService.store(file, "lost-items");
        boolean isFirst = item.getPhotos().isEmpty();
        ItemPhoto photo = ItemPhoto.builder()
                .lostItem(item)
                .fileUrl(url)
                .isPrimary(isFirst)
                .visibility(PhotoVisibility.PUBLIC)
                .uploadedBy(item.getOwner())
                .build();
        photo = itemPhotoRepository.save(photo);
        return PhotoUploadResponse.builder()
                .photoId(photo.getPhotoId())
                .fileUrl(photo.getFileUrl())
                .message("Photo uploaded successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public List<LostItemSummaryDto> listByStatus(LostItemStatus status) {
        return lostItemRepository.findByStatus(status).stream()
                .map(item -> lostItemMapper.toSummaryDto(item, caseRepository.findByLostItem_LostItemId(item.getLostItemId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LostItemSummaryDto> getPoliceLostReports(UserPrincipal principal, LostItemStatus status) {
        assertPolice(principal);
        UUID stationId = principal.getStationId();
        if (stationId == null) {
            throw new ForbiddenException("Your police account is not assigned to a station");
        }
        List<LostItem> reports = new java.util.ArrayList<>(status == null
                ? lostItemRepository.findByAssignedStation_StationIdOrderByCreatedAtDesc(stationId)
                : lostItemRepository.findByAssignedStation_StationIdAndStatusOrderByCreatedAtDesc(stationId, status));
        reports.addAll(status == null
                ? lostItemRepository.findByAssignedStationIsNullOrderByCreatedAtDesc()
                : lostItemRepository.findByAssignedStationIsNullAndStatusOrderByCreatedAtDesc(status));
        reports.sort(java.util.Comparator.comparing(LostItem::getCreatedAt).reversed());
        return reports.stream()
                .map(item -> lostItemMapper.toSummaryDto(item, caseRepository.findByLostItem_LostItemId(item.getLostItemId()).orElse(null)))
                .toList();
    }

    @Transactional(readOnly = true)
    public LostItemDetailDto getPoliceLostReport(UUID lostItemId, UserPrincipal principal) {
        LostItem item = getEntityById(lostItemId);
        assertCanView(item, principal);
        Case linkedCase = caseRepository.findByLostItem_LostItemId(lostItemId).orElse(null);
        return lostItemMapper.toDetailDto(item, linkedCase);
    }

    @Transactional
    public LostItemDetailDto acknowledgePoliceReceipt(UUID lostItemId, User officer) {
        LostItem item = getEntityById(lostItemId);
        assertStationCanAccess(item.getAssignedStation(), officer);
        if (item.getStatus() != LostItemStatus.REPORTED) {
            throw new BadRequestException("Only reports awaiting police receipt can be acknowledged");
        }

        if (item.getAssignedStation() == null) {
            item.setAssignedStation(officer.getStation());
        }
        item.setStatus(LostItemStatus.RECEIVED);
        item = lostItemRepository.save(item);
        caseService.findAndTransition(item, com.lostandfound.entity.enums.CaseStatus.RECEIVED,
                officer, "Digital lost-item report received by police");
        auditService.log(officer, "LOST_REPORT_RECEIVED_BY_POLICE", "LostItem", item.getLostItemId());
        notificationService.notify(item.getOwner(), NotificationType.GENERAL,
                "Police received your lost-item report",
                "Police have received your report" + caseRepository.findByLostItem_LostItemId(item.getLostItemId())
                        .map(linkedCase -> " (case " + linkedCase.getCaseNumber() + ")")
                        .orElse("") + ". It remains active for matching and review.",
                null, null);

        Case linkedCase = caseRepository.findByLostItem_LostItemId(item.getLostItemId()).orElse(null);
        return lostItemMapper.toDetailDto(item, linkedCase);
    }

    public LostItem getEntityById(UUID lostItemId) {
        return lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found: " + lostItemId));
    }

    private void assertOwner(LostItem item, UserPrincipal principal) {
        if (!item.getOwner().getUserId().equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have permission to modify this lost-item report");
        }
    }

    private void assertCanView(LostItem item, UserPrincipal principal) {
        boolean isOwner = item.getOwner().getUserId().equals(principal.getUserId());
        if (isOwner || "SYSTEM_ADMIN".equals(principal.getRole())) return;
        if ("POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole())) {
            assertStationCanAccess(item.getAssignedStation(), principal.getStationId());
            return;
        }
        if (!isOwner) {
            throw new ForbiddenException("You do not have permission to view this lost-item report");
        }
    }

    private void assertPolice(UserPrincipal principal) {
        if (!"POLICE_OFFICER".equals(principal.getRole()) && !"POLICE_ADMIN".equals(principal.getRole())) {
            throw new ForbiddenException("Only police personnel can browse lost-item reports");
        }
    }

    private void assertStationCanAccess(PoliceStation assignedStation, User officer) {
        if (officer.getStation() == null) {
            throw new ForbiddenException("Your police account is not assigned to a station");
        }
        if (assignedStation != null && !assignedStation.getStationId().equals(officer.getStation().getStationId())) {
            throw new ForbiddenException("This report is assigned to another police station");
        }
    }

    private void assertStationCanAccess(PoliceStation assignedStation, UUID officerStationId) {
        if (officerStationId == null) {
            throw new ForbiddenException("Your police account is not assigned to a station");
        }
        if (assignedStation != null && !assignedStation.getStationId().equals(officerStationId)) {
            throw new ForbiddenException("This report is assigned to another police station");
        }
    }
}