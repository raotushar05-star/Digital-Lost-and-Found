package com.lostandfound.service;

import com.lostandfound.dto.station.StationCreateRequest;
import com.lostandfound.dto.station.StationDto;
import com.lostandfound.dto.station.StationUpdateRequest;
import com.lostandfound.entity.Location;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.exception.ConflictException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.StationMapper;
import com.lostandfound.repository.PoliceStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final PoliceStationRepository stationRepository;
    private final StationMapper stationMapper;
    private final LocationService locationService;

    public List<StationDto> getAllStations() {
        return stationRepository.findAll().stream().map(stationMapper::toDto).collect(Collectors.toList());
    }

    public StationDto getById(UUID stationId) {
        return stationMapper.toDto(getEntityById(stationId));
    }

    public PoliceStation getEntityById(UUID stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Police station not found: " + stationId));
    }

    @Transactional
    public StationDto create(StationCreateRequest request) {
        if (stationRepository.existsByStationCode(request.getStationCode())) {
            throw new ConflictException("A station with this code already exists");
        }
        Location location = request.getLocation() != null ? locationService.createFromDto(request.getLocation()) : null;
        PoliceStation station = PoliceStation.builder()
                .stationName(request.getStationName())
                .stationCode(request.getStationCode())
                .address(request.getAddress())
                .phone(request.getPhone())
                .location(location)
                .isActive(true)
                .build();
        return stationMapper.toDto(stationRepository.save(station));
    }

    @Transactional
    public StationDto update(UUID stationId, StationUpdateRequest request) {
        PoliceStation station = getEntityById(stationId);
        station.setStationName(request.getStationName());
        station.setAddress(request.getAddress());
        station.setPhone(request.getPhone());
        if (request.getIsActive() != null) {
            station.setIsActive(request.getIsActive());
        }
        if (request.getLocation() != null) {
            Location location = locationService.createFromDto(request.getLocation());
            station.setLocation(location);
        }
        return stationMapper.toDto(stationRepository.save(station));
    }
}
