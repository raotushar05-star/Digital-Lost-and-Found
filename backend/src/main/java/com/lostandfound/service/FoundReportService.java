package com.lostandfound.service;

import com.lostandfound.dto.foundreport.FoundReportCreateRequest;
import com.lostandfound.dto.foundreport.FoundReportCreateResponse;
import com.lostandfound.dto.foundreport.FoundReportDto;
import com.lostandfound.entity.Category;
import com.lostandfound.entity.FoundReport;
import com.lostandfound.entity.ItemPhoto;
import com.lostandfound.entity.Location;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.FoundReportStatus;
import com.lostandfound.entity.enums.PhotoVisibility;
import com.lostandfound.exception.BadRequestException;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.FoundReportMapper;
import com.lostandfound.mapper.PhotoMapper;
import com.lostandfound.repository.CategoryRepository;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.repository.FoundReportRepository;
import com.lostandfound.repository.ItemPhotoRepository;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.util.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Handles preliminary finder submissions only. A found_report is NOT an
 * official custody record - only an authorized police officer creates the
 * corresponding found_item (see FoundItemService).
 *
 * A photo is required at submission time: it strengthens the proof-of-discovery
 * trail (timestamp + location + photo) captured before the item ever reaches
 * police custody, which matters for deterring a finder from simply keeping an
 * item instead of reporting it. This does not - and cannot - guarantee honesty,
 * but it creates an auditable record of what was found, where, and when.
 */
@Service
@RequiredArgsConstructor
public class FoundReportService {

    private final FoundReportRepository foundReportRepository;
    private final FoundItemRepository foundItemRepository;
    private final CategoryRepository categoryRepository;
    private final ItemPhotoRepository itemPhotoRepository;
    private final LocationService locationService;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final FoundReportMapper foundReportMapper;
    private final PhotoMapper photoMapper;

    @Transactional
    public FoundReportCreateResponse create(User finder, FoundReportCreateRequest request, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new BadRequestException("A photo of the found item is required when submitting a report");
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        Location location = locationService.createFromDto(request.getLocation());

        FoundReport report = FoundReport.builder()
                .finder(finder)
                .category(category)
                .description(request.getDescription())
                .brand(request.getBrand())
                .color(request.getColor())
                .foundDate(request.getFoundDate())
                .location(location)
                .status(FoundReportStatus.SUBMITTED)
                .build();
        report = foundReportRepository.save(report);

        String photoUrl = fileStorageService.store(photo, "found-reports");
        ItemPhoto itemPhoto = ItemPhoto.builder()
                .foundReport(report)
                .fileUrl(photoUrl)
                .isPrimary(true)
                .visibility(PhotoVisibility.PUBLIC)
                .uploadedBy(finder)
                .build();
        itemPhotoRepository.save(itemPhoto);

        auditService.log(finder, "FOUND_REPORT_SUBMITTED", "FoundReport", report.getFoundReportId());

        return FoundReportCreateResponse.builder()
                .foundReportId(report.getFoundReportId())
                .status(report.getStatus().name())
                .message("Found-item report submitted successfully")
                .build();
    }

    public FoundReportDto getById(UUID foundReportId, UserPrincipal principal) {
        FoundReport report = getEntityById(foundReportId);
        boolean isFinder = report.getFinder().getUserId().equals(principal.getUserId());
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (!isFinder && !isPolice) {
            throw new ForbiddenException("You do not have permission to view this found report");
        }
        UUID linkedFoundItemId = foundItemRepository.findByFoundReport_FoundReportId(foundReportId)
                .map(fi -> fi.getFoundItemId())
                .orElse(null);
        List<com.lostandfound.dto.file.PhotoDto> photos = photoMapper.toDtoList(
                itemPhotoRepository.findByFoundReport_FoundReportId(foundReportId));
        return foundReportMapper.toDto(report, linkedFoundItemId, photos);
    }

    public FoundReport getEntityById(UUID foundReportId) {
        return foundReportRepository.findById(foundReportId)
                .orElseThrow(() -> new ResourceNotFoundException("Found report not found: " + foundReportId));
    }
}