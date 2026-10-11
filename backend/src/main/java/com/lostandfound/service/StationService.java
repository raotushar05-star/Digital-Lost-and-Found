package com.lostandfound.service;

import com.lostandfound.dto.station.StationAdminCredentialsDto;
import com.lostandfound.dto.station.StationCreateRequest;
import com.lostandfound.dto.station.StationCreateResponse;
import com.lostandfound.dto.station.StationDto;
import com.lostandfound.dto.station.StationUpdateRequest;
import com.lostandfound.entity.Location;
import com.lostandfound.entity.PoliceStation;
import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.Role;
import com.lostandfound.exception.ConflictException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.mapper.StationMapper;
import com.lostandfound.repository.PoliceStationRepository;
import com.lostandfound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final PoliceStationRepository stationRepository;
    private final UserRepository userRepository;
    private final StationMapper stationMapper;
    private final LocationService locationService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<StationDto> getAllStations() {
        return stationRepository.findAll().stream().map(stationMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StationDto getById(UUID stationId) {
        return stationMapper.toDto(getEntityById(stationId));
    }

    public PoliceStation getEntityById(UUID stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Police station not found: " + stationId));
    }

    /**
     * Creates a station and a POLICE_ADMIN login bound to it.
     * Safe for demos: only SYSTEM_ADMIN can call this; public registration stays USER-only.
     */
    @Transactional
    public StationCreateResponse create(StationCreateRequest request) {
        String adminEmail = request.getAdminEmail().trim().toLowerCase();
        if (stationRepository.existsByStationCode(request.getStationCode())) {
            throw new ConflictException("A station with this code already exists");
        }
        if (userRepository.existsByEmail(adminEmail)) {
            throw new ConflictException("An account with this police admin email already exists");
        }
        if (userRepository.existsByPhone(request.getAdminPhone())) {
            throw new ConflictException("An account with this police admin phone already exists");
        }

        Location location = locationService.createFromDto(request.getLocation());
        PoliceStation station = stationRepository.save(PoliceStation.builder()
                .stationName(request.getStationName())
                .stationCode(request.getStationCode())
                .address(request.getAddress())
                .phone(request.getPhone())
                .location(location)
                .isActive(true)
                .build());

        User policeAdmin = userRepository.save(User.builder()
                .name(request.getAdminName())
                .email(adminEmail)
                .phone(request.getAdminPhone())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .role(Role.POLICE_ADMIN)
                .station(station)
                .isActive(true)
                .build());
        auditService.log(policeAdmin, "STATION_ADMIN_PROVISIONED", "PoliceStation", station.getStationId());

        StationAdminCredentialsDto credentials = StationAdminCredentialsDto.builder()
                .name(policeAdmin.getName())
                .email(policeAdmin.getEmail())
                .phone(policeAdmin.getPhone())
                .role(policeAdmin.getRole().name())
                .build();

        return StationCreateResponse.builder()
                .station(stationMapper.toDto(station))
                .policeAdmin(credentials)
                .message("Station created. Sign in with the police admin email and the password you set.")
                .build();
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
