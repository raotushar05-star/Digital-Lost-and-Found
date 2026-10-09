package com.lostandfound.service;

import com.lostandfound.dto.lostitem.LostItemDetailDto;
import com.lostandfound.dto.lostitem.LostItemCreateRequest;
import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.entity.Case;
import com.lostandfound.entity.Category;
import com.lostandfound.entity.Location;
import com.lostandfound.entity.LostItem;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.CaseStatus;
import com.lostandfound.entity.enums.LostItemStatus;
import com.lostandfound.entity.enums.NotificationType;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.mapper.LostItemMapper;
import com.lostandfound.repository.CaseRepository;
import com.lostandfound.repository.CategoryRepository;
import com.lostandfound.repository.ItemPhotoRepository;
import com.lostandfound.repository.LostItemRepository;
import com.lostandfound.repository.UserRepository;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.util.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LostItemServiceTest {

    @Mock
    private LostItemRepository lostItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private ItemPhotoRepository itemPhotoRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private CaseService caseService;

    @Mock
    private MatchingService matchingService;

        @Mock
        private StationAssignmentService stationAssignmentService;

    @Mock
    private NotificationService notificationService;

        @Mock
        private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuditService auditService;

    @Mock
    private LostItemMapper lostItemMapper;

    @InjectMocks
    private LostItemService lostItemService;

    @Test
    void create_shouldNotifyActivePoliceAtAssignedStation() {
        User owner = user(Role.USER, "Owner", "owner@example.test", "1111111111");
        User officer = user(Role.POLICE_OFFICER, "Barke Officer", "officer@example.test", "2222222222");
        User stationAdmin = user(Role.POLICE_ADMIN, "Barke Admin", "admin@example.test", "3333333333");
        PoliceStation station = PoliceStation.builder()
                .stationId(UUID.randomUUID())
                .stationName("Barke Police Station")
                .build();
        officer.setStation(station);
        stationAdmin.setStation(station);
        Category category = Category.builder()
                .categoryId(UUID.randomUUID())
                .categoryName("Keys")
                .isActive(true)
                .build();
        LocationDto locationDto = LocationDto.builder()
                .latitude(new BigDecimal("12.900000"))
                .longitude(new BigDecimal("74.850000"))
                .city("Mangaluru")
                .build();
        Location location = Location.builder()
                .locationId(UUID.randomUUID())
                .latitude(locationDto.getLatitude())
                .longitude(locationDto.getLongitude())
                .city(locationDto.getCity())
                .build();
        Case linkedCase = Case.builder()
                .caseId(UUID.randomUUID())
                .caseNumber("LF-2026-010")
                .build();
        LostItemCreateRequest request = LostItemCreateRequest.builder()
                .categoryId(category.getCategoryId())
                .description("Door-lock keys")
                .lostDate(LocalDate.now())
                .location(locationDto)
                .build();

        when(categoryRepository.findById(category.getCategoryId())).thenReturn(Optional.of(category));
        when(locationService.createFromDto(locationDto)).thenReturn(location);
        when(stationAssignmentService.findNearestStation(location)).thenReturn(Optional.of(station));
        when(lostItemRepository.save(any(LostItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(caseService.createForLostItem(any(LostItem.class), eq(owner))).thenReturn(linkedCase);
        when(userRepository.findByStation_StationIdAndRoleIn(
                eq(station.getStationId()), eq(List.of(Role.POLICE_OFFICER, Role.POLICE_ADMIN))))
                .thenReturn(List.of(officer, stationAdmin));

        lostItemService.create(owner.getUserId(), request, owner);

        verify(notificationService, times(2)).notify(
                any(User.class),
                eq(NotificationType.GENERAL),
                eq("New lost-item report assigned"),
                contains("LF-2026-010"),
                eq(linkedCase),
                isNull()
        );
    }

    @Test
    void acknowledgePoliceReceipt_shouldUpdateCaseAuditAndNotifyOwner() {
        UUID lostItemId = UUID.randomUUID();
        User owner = user(Role.USER, "Owner", "owner@example.test", "1111111111");
        PoliceStation station = PoliceStation.builder().stationId(UUID.randomUUID()).stationName("Mangaluru Station").build();
        User officer = user(Role.POLICE_OFFICER, "Officer", "officer@example.test", "2222222222");
        officer.setStation(station);
        LostItem lostItem = LostItem.builder()
                .lostItemId(lostItemId)
                .owner(owner)
                .category(Category.builder().categoryId(UUID.randomUUID()).categoryName("Wallet").build())
                .description("Black wallet")
                .lostDate(LocalDate.now())
                .location(Location.builder().locationId(UUID.randomUUID()).city("Mangaluru").build())
                .status(LostItemStatus.REPORTED)
                .build();
        Case linkedCase = Case.builder().caseNumber("LF-2026-001").build();
        LostItemDetailDto expected = LostItemDetailDto.builder().lostItemId(lostItemId).status("RECEIVED").build();

        when(lostItemRepository.findById(lostItemId)).thenReturn(Optional.of(lostItem));
        when(lostItemRepository.save(lostItem)).thenReturn(lostItem);
        when(caseRepository.findByLostItem_LostItemId(lostItemId)).thenReturn(Optional.of(linkedCase));
        when(lostItemMapper.toDetailDto(lostItem, linkedCase)).thenReturn(expected);

        LostItemDetailDto result = lostItemService.acknowledgePoliceReceipt(lostItemId, officer);

        assertEquals(LostItemStatus.RECEIVED, lostItem.getStatus());
        assertEquals(expected, result);
        verify(caseService).findAndTransition(lostItem, CaseStatus.RECEIVED, officer, "Digital lost-item report received by police");
        verify(auditService).log(officer, "LOST_REPORT_RECEIVED_BY_POLICE", "LostItem", lostItemId);
        verify(notificationService).notify(
                eq(owner),
                eq(NotificationType.GENERAL),
                eq("Police received your lost-item report"),
                contains("LF-2026-001"),
                isNull(),
                isNull()
        );
    }

    @Test
    void acknowledgePoliceReceipt_shouldRejectReportsThatAreNotNew() {
        UUID lostItemId = UUID.randomUUID();
        LostItem lostItem = LostItem.builder()
                .lostItemId(lostItemId)
                .status(LostItemStatus.RECEIVED)
                .build();
        User officer = user(Role.POLICE_OFFICER, "Officer", "officer@example.test", "2222222222");
        officer.setStation(PoliceStation.builder().stationId(UUID.randomUUID()).stationName("Mangaluru Station").build());
        when(lostItemRepository.findById(lostItemId)).thenReturn(Optional.of(lostItem));

        assertThrows(com.lostandfound.exception.BadRequestException.class,
                () -> lostItemService.acknowledgePoliceReceipt(lostItemId, officer));

        verify(lostItemRepository, never()).save(any(LostItem.class));
        verify(notificationService, never()).notify(any(), any(), any(), any(), any(), any());
    }

        @Test
        void withdraw_shouldRejectAfterPoliceReceivesReport() {
                UUID lostItemId = UUID.randomUUID();
                User owner = user(Role.USER, "Owner", "owner@example.test", "1111111111");
                LostItem lostItem = LostItem.builder()
                                .lostItemId(lostItemId)
                                .owner(owner)
                                .status(LostItemStatus.RECEIVED)
                                .build();
                when(lostItemRepository.findById(lostItemId)).thenReturn(Optional.of(lostItem));

                assertThrows(com.lostandfound.exception.BadRequestException.class,
                                () -> lostItemService.withdraw(lostItemId, new UserPrincipal(owner)));

                verify(lostItemRepository, never()).save(any(LostItem.class));
                verify(auditService, never()).log(any(), any(), any(), any());
        }

        @Test
        void withdraw_shouldAllowReportBeforePoliceReceivesIt() {
                UUID lostItemId = UUID.randomUUID();
                User owner = user(Role.USER, "Owner", "owner@example.test", "1111111111");
                LostItem lostItem = LostItem.builder()
                                .lostItemId(lostItemId)
                                .owner(owner)
                                .status(LostItemStatus.POTENTIAL_MATCH)
                                .build();
                when(lostItemRepository.findById(lostItemId)).thenReturn(Optional.of(lostItem));

                lostItemService.withdraw(lostItemId, new UserPrincipal(owner));

                assertEquals(LostItemStatus.WITHDRAWN, lostItem.getStatus());
                verify(lostItemRepository).save(lostItem);
                verify(auditService).log(owner, "LOST_ITEM_WITHDRAWN", "LostItem", lostItemId);
        }

    private User user(Role role, String name, String email, String phone) {
        return User.builder()
                .userId(UUID.randomUUID())
                .name(name)
                .email(email)
                .phone(phone)
                .passwordHash("hashed")
                .role(role)
                .isActive(true)
                .build();
    }
}
