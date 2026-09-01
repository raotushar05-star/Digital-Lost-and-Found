package com.lostandfound.service;

import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.entity.Location;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.LocationMapper;
import com.lostandfound.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Transactional
    public Location createFromDto(LocationDto dto) {
        Location location = locationMapper.toEntity(dto);
        return locationRepository.save(location);
    }

    public Location getById(UUID locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + locationId));
    }

    public LocationDto getDtoById(UUID locationId) {
        return locationMapper.toDto(getById(locationId));
    }

    /** Great-circle distance between two points in kilometers (Haversine formula). */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
