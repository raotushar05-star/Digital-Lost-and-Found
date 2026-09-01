package com.lostandfound.mapper;

import com.lostandfound.dto.station.StationDto;
import com.lostandfound.entity.PoliceStation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StationMapper {

    private final LocationMapper locationMapper;

    public StationDto toDto(PoliceStation station) {
        if (station == null) return null;
        return StationDto.builder()
                .stationId(station.getStationId())
                .stationName(station.getStationName())
                .stationCode(station.getStationCode())
                .address(station.getAddress())
                .phone(station.getPhone())
                .location(locationMapper.toDto(station.getLocation()))
                .isActive(station.getIsActive())
                .createdAt(station.getCreatedAt())
                .build();
    }
}
