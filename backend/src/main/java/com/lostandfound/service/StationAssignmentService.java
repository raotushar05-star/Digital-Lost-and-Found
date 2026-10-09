package com.lostandfound.service;

import com.lostandfound.entity.Location;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.repository.PoliceStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StationAssignmentService {

    private final PoliceStationRepository stationRepository;

    public Optional<PoliceStation> findNearestStation(Location reportLocation) {
        if (reportLocation == null || reportLocation.getLatitude() == null || reportLocation.getLongitude() == null) {
            return Optional.empty();
        }
        return stationRepository.findByIsActiveTrue().stream()
                .filter(station -> station.getLocation() != null
                        && station.getLocation().getLatitude() != null
                        && station.getLocation().getLongitude() != null)
                .min(Comparator.comparingDouble(station -> LocationService.distanceKm(
                        reportLocation.getLatitude().doubleValue(), reportLocation.getLongitude().doubleValue(),
                        station.getLocation().getLatitude().doubleValue(), station.getLocation().getLongitude().doubleValue())));
    }
}