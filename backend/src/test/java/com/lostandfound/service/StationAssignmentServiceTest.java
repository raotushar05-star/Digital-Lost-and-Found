package com.lostandfound.service;

import com.lostandfound.entity.Location;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.repository.PoliceStationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationAssignmentServiceTest {

    @Mock
    private PoliceStationRepository stationRepository;

    @InjectMocks
    private StationAssignmentService stationAssignmentService;

    @Test
    void findNearestStation_returnsClosestActiveLocatedStation() {
        PoliceStation bengaluru = station("Bengaluru", 12.9716, 77.5946);
        PoliceStation mangaluru = station("Mangaluru", 12.9141, 74.8560);
        PoliceStation missingLocation = PoliceStation.builder()
                .stationId(UUID.randomUUID())
                .stationName("Unlocated station")
                .build();
        Location reportLocation = location(12.9150, 74.8550);
        when(stationRepository.findByIsActiveTrue()).thenReturn(List.of(bengaluru, missingLocation, mangaluru));

        Optional<PoliceStation> result = stationAssignmentService.findNearestStation(reportLocation);

        assertTrue(result.isPresent());
        assertEquals(mangaluru.getStationId(), result.get().getStationId());
    }

    @Test
    void findNearestStation_returnsEmptyWhenNoStationHasCoordinates() {
        when(stationRepository.findByIsActiveTrue()).thenReturn(List.of(
                PoliceStation.builder().stationId(UUID.randomUUID()).stationName("Unlocated station").build()));

        assertTrue(stationAssignmentService.findNearestStation(location(12.9, 74.8)).isEmpty());
    }

    private PoliceStation station(String name, double latitude, double longitude) {
        return PoliceStation.builder()
                .stationId(UUID.randomUUID())
                .stationName(name)
                .location(location(latitude, longitude))
                .isActive(true)
                .build();
    }

    private Location location(double latitude, double longitude) {
        return Location.builder()
                .locationId(UUID.randomUUID())
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .city("Test city")
                .build();
    }
}
