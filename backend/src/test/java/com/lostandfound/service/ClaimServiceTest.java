package com.lostandfound.service;

import com.lostandfound.dto.claim.ClaimCreateRequest;
import com.lostandfound.entity.*;
import com.lostandfound.entity.enums.*;
import com.lostandfound.mapper.ClaimMapper;
import com.lostandfound.repository.ClaimRepository;
import com.lostandfound.repository.FoundItemRepository;
import com.lostandfound.repository.LostItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private FoundItemRepository foundItemRepository;

    @Mock
    private LostItemRepository lostItemRepository;

    @Mock
    private CaseService caseService;

    @Mock
    private AuditService auditService;

    @Mock
    private ClaimMapper claimMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimService claimService;

    @Test
    void create_shouldNotifyFinderAndClaimant() {
        User claimant = User.builder()
                .userId(UUID.randomUUID())
                .name("Owner")
                .email("owner@lostandfound.local")
                .phone("1111111111")
                .passwordHash("hash")
                .role(Role.USER)
                .isActive(true)
                .build();

        User finder = User.builder()
                .userId(UUID.randomUUID())
                .name("Finder")
                .email("finder@lostandfound.local")
                .phone("2222222222")
                .passwordHash("hash")
                .role(Role.USER)
                .isActive(true)
                .build();

        Category category = Category.builder()
                .categoryId(UUID.randomUUID())
                .categoryName("Wallet")
                .description("Wallets")
                .isActive(true)
                .build();

        FoundReport report = FoundReport.builder()
                .foundReportId(UUID.randomUUID())
                .finder(finder)
                .category(category)
                .description("Black wallet")
                .foundDate(LocalDate.now())
                .location(Location.builder().locationId(UUID.randomUUID()).city("Bengaluru").state("Karnataka").build())
                .status(FoundReportStatus.SUBMITTED)
                .build();

        FoundItem foundItem = FoundItem.builder()
                .foundItemId(UUID.randomUUID())
                .foundReport(report)
                .category(category)
                .description("Black wallet")
                .brand("Hidesign")
                .color("Black")
                .foundDate(LocalDate.now())
                .location(report.getLocation())
                .verificationStatus(FoundItemVerificationStatus.VERIFIED)
                .custodyStatus(CustodyStatus.IN_CUSTODY)
                .receivedDate(LocalDateTime.now())
                .build();

        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .foundItem(foundItem)
                .claimant(claimant)
                .claimDetails("This is my wallet")
                .status(ClaimStatus.PENDING)
                .build();

        when(foundItemRepository.findById(foundItem.getFoundItemId())).thenReturn(Optional.of(foundItem));
        when(claimRepository.findByFoundItem_FoundItemIdOrderByCreatedAtDesc(foundItem.getFoundItemId())).thenReturn(List.of());
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        claimService.create(foundItem.getFoundItemId(), claimant, ClaimCreateRequest.builder()
                .claimDetails("This is my wallet")
                .build());

        verify(notificationService).notify(eq(finder), eq(NotificationType.CLAIM_SUBMITTED), anyString(), anyString(), isNull(), isNull());
        verify(notificationService).notify(eq(claimant), eq(NotificationType.CLAIM_SUBMITTED), anyString(), anyString(), isNull(), isNull());
    }
}
