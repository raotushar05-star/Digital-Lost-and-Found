package com.lostandfound.service;

import com.lostandfound.entity.Location;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.repository.PoliceStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StationAssignmentService {

    private final PoliceStationRepository stationRepository;

    /** Reports farther than this from every station stay unassigned. */
    @Value("${app.routing.max-distance-km:30}")
    private double maxDistanceKm = 30.0;

    public Optional<PoliceStation> findNearestStation(Location reportLocation) {
        if (reportLocation == null || reportLocation.getLatitude() == null || reportLocation.getLongitude() == null) {
            return Optional.empty();
        }
        double lat = reportLocation.getLatitude().doubleValue();
        double lon = reportLocation.getLongitude().doubleValue();
        return stationRepository.findByIsActiveTrue().stream()
                .filter(station -> station.getLocation() != null
                        && station.getLocation().getLatitude() != null
                        && station.getLocation().getLongitude() != null)
                .map(station -> Map.entry(station, LocationService.distanceKm(lat, lon,
                        station.getLocation().getLatitude().doubleValue(),
                        station.getLocation().getLongitude().doubleValue())))
                .filter(entry -> entry.getValue() <= maxDistanceKm)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}