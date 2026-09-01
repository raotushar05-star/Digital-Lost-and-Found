package com.lostandfound.mapper;

import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationDto toDto(Location location) {
        if (location == null) return null;
        return LocationDto.builder()
                .locationId(location.getLocationId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .addressText(location.getAddressText())
                .locality(location.getLocality())
                .city(location.getCity())
                .state(location.getState())
                .postalCode(location.getPostalCode())
                .build();
    }

    public Location toEntity(LocationDto dto) {
        if (dto == null) return null;
        return Location.builder()
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .addressText(dto.getAddressText())
                .locality(dto.getLocality())
                .city(dto.getCity())
                .state(dto.getState())
                .postalCode(dto.getPostalCode())
                .build();
    }
}
