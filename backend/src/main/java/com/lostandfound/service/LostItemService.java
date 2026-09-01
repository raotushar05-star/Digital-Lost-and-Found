package com.lostandfound.service;

import com.lostandfound.dto.file.PhotoUploadResponse;
import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.dto.lostitem.*;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.LostItemStatus;
import com.lostandfound.entity.enums.PhotoVisibility;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.LostItemMapper;
import com.lostandfound.repository.CaseRepository;
import com.lostandfound.repository.CategoryRepository;
import com.lostandfound.repository.ItemPhotoRepository;
import com.lostandfound.repository.LostItemRepository;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LostItemService {

    private static final Set<LostItemStatus> EDITABLE_STATUSES = Set.of(
            LostItemStatus.REPORTED, LostItemStatus.POTENTIAL_MATCH);

    private final LostItemRepository lostItemRepository;
    private final CategoryRepository categoryRepository;
    private final CaseRepository caseRepository;
    private final ItemPhotoRepository itemPhotoRepository;
    private final LocationService locationService;
    private final CaseService caseService;
    private final MatchingService matchingService;
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
                .status(LostItemStatus.REPORTED)
                .build();
        lostItem = lostItemRepository.save(lostItem);

        caseService.createForLostItem(lostItem, owner);
        auditService.log(owner, "LOST_ITEM_REPORTED", "LostItem", lostItem.getLostItemId());
        matchingService.generateForLostItem(lostItem);

        return LostItemCreateResponse.builder()
                .lostItemId(lostItem.getLostItemId())
                .status(lostItem.getStatus().name())
                .message("Lost item reported successfully")
                .build();
    }

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
        if (item.getStatus() == LostItemStatus.RESOLVED || item.getStatus() == LostItemStatus.RETURNED) {
            throw new BadRequestException("A resolved or returned lost-item report cannot be withdrawn");
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

    public List<LostItemSummaryDto> listByStatus(LostItemStatus status) {
        return lostItemRepository.findByStatus(status).stream()
                .map(item -> lostItemMapper.toSummaryDto(item, caseRepository.findByLostItem_LostItemId(item.getLostItemId()).orElse(null)))
                .toList();
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
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (!isOwner && !isPolice) {
            throw new ForbiddenException("You do not have permission to view this lost-item report");
        }
    }
}
