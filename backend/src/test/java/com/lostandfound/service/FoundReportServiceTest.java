package com.lostandfound.service;

import com.lostandfound.dto.foundreport.FoundReportCreateRequest;
import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.entity.Category;
import com.lostandfound.entity.FoundReport;
import com.lostandfound.entity.Location;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.FoundReportStatus;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.mapper.FoundReportMapper;
import com.lostandfound.mapper.PhotoMapper;
import com.lostandfound.repository.CategoryRepository;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.repository.FoundReportRepository;
import com.lostandfound.repository.ItemPhotoRepository;
import com.lostandfound.util.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoundReportServiceTest {

    @Mock
    private FoundReportRepository foundReportRepository;

    @Mock
    private FoundItemRepository foundItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemPhotoRepository itemPhotoRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuditService auditService;

    @Mock
    private FoundReportMapper foundReportMapper;

    @Mock
    private PhotoMapper photoMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FoundReportService foundReportService;

    @Test
    void create_shouldNotifyFinderAfterSubmittingFoundReport() {
        User finder = User.builder()
                .userId(UUID.randomUUID())
                .name("Citizen Test")
                .email("citizen@lostandfound.local")
                .phone("1234567890")
                .passwordHash("hashed")
                .role(Role.USER)
                .isActive(true)
                .build();

        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .categoryId(categoryId)
                .categoryName("Wallet")
                .description("Wallets")
                .isActive(true)
                .build();

        Location location = Location.builder()
                .locationId(UUID.randomUUID())
                .city("Bengaluru")
                .state("Karnataka")
                .build();

        FoundReportCreateRequest request = FoundReportCreateRequest.builder()
                .categoryId(categoryId)
                .description("Black leather wallet")
                .brand("Hidesign")
                .color("Black")
                .foundDate(LocalDate.now())
                .location(LocationDto.builder().city("Bengaluru").state("Karnataka").build())
                .build();

        FoundReport savedReport = FoundReport.builder()
                .foundReportId(UUID.randomUUID())
                .finder(finder)
                .category(category)
                .description("Black leather wallet")
                .brand("Hidesign")
                .color("Black")
                .foundDate(LocalDate.now())
                .location(location)
                .status(FoundReportStatus.SUBMITTED)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(locationService.createFromDto(request.getLocation())).thenReturn(location);
        when(foundReportRepository.save(any(FoundReport.class))).thenReturn(savedReport);
        when(fileStorageService.store(any(), eq("found-reports"))).thenReturn("uploads/found-reports/test.jpg");

        foundReportService.create(finder, request, new MockMultipartFile(
                "photo", "wallet.jpg", "image/jpeg", "dummy-image".getBytes()));

        verify(notificationService).notify(
                eq(finder),
                eq(NotificationType.GENERAL),
                eq("Found item report received"),
                contains("submitted successfully"),
                isNull(),
                isNull()
        );
    }
}
