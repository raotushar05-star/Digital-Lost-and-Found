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
import com.lostandfound.util.IdentityNormalizer;
import com.lostandfound.exception.BadRequestException;

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
     * Creates a station and a POLICE_OFFICER login bound to it. Only SYSTEM_ADMIN
     * can call this.
     * One transaction: if any step fails, nothing is saved.
     */
    @Transactional
    public StationCreateResponse create(StationCreateRequest request) {
        String officerEmail = IdentityNormalizer.email(request.getOfficerEmail());
        String officerPhone = IdentityNormalizer.phone(request.getOfficerPhone());
        if (!IdentityNormalizer.isValidPhone(officerPhone)) {
            throw new BadRequestException("Officer phone is invalid");
        }
        if (stationRepository.existsByStationCode(request.getStationCode())) {
            throw new ConflictException("A station with this code already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(officerEmail)) {
            throw new ConflictException("An account with this officer email already exists");
        }
        if (userRepository.existsByPhone(officerPhone)) {
            throw new ConflictException("An account with this officer phone already exists");
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

        User officer = userRepository.save(User.builder()
                .name(request.getOfficerName())
                .email(officerEmail)
                .phone(request.getOfficerPhone())
                .passwordHash(passwordEncoder.encode(request.getOfficerPassword()))
                .role(Role.POLICE_OFFICER)
                .station(station)
                .isActive(true)
                .build());
        auditService.log(officer, "STATION_OFFICER_PROVISIONED", "PoliceStation", station.getStationId());

        return StationCreateResponse.builder()
                .station(stationMapper.toDto(station))
                .policeOfficer(StationAdminCredentialsDto.builder()
                        .name(officer.getName())
                        .email(officer.getEmail())
                        .phone(officer.getPhone())
                        .role(officer.getRole().name())
                        .build())
                .message("Station created with an officer login.")
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