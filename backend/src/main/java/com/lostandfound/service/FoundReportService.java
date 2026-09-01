package com.lostandfound.service;

import com.lostandfound.dto.foundreport.FoundReportCreateRequest;
import com.lostandfound.dto.foundreport.FoundReportCreateResponse;
import com.lostandfound.dto.foundreport.FoundReportDto;
import com.lostandfound.entity.Category;
import com.lostandfound.entity.FoundReport;
import com.lostandfound.entity.Location;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.FoundReportStatus;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.FoundReportMapper;
import com.lostandfound.repository.CategoryRepository;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.repository.FoundReportRepository;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles preliminary finder submissions only. A found_report is NOT an
 * official custody record - only an authorized police officer creates the
 * corresponding found_item (see FoundItemService).
 */
@Service
@RequiredArgsConstructor
public class FoundReportService {

    private final FoundReportRepository foundReportRepository;
    private final FoundItemRepository foundItemRepository;
    private final CategoryRepository categoryRepository;
    private final LocationService locationService;
    private final AuditService auditService;
    private final FoundReportMapper foundReportMapper;

    @Transactional
    public FoundReportCreateResponse create(User finder, FoundReportCreateRequest request) {
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
        return foundReportMapper.toDto(report, linkedFoundItemId);
    }

    public FoundReport getEntityById(UUID foundReportId) {
        return foundReportRepository.findById(foundReportId)
                .orElseThrow(() -> new ResourceNotFoundException("Found report not found: " + foundReportId));
    }
}
