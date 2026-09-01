package com.lostandfound.controller;

import com.lostandfound.dto.location.LocationDto;
import com.lostandfound.entity.Location;
import com.lostandfound.mapper.LocationMapper;
import com.lostandfound.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @PostMapping
    public ResponseEntity<LocationDto> create(@Valid @RequestBody LocationDto request) {
        Location location = locationService.createFromDto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(locationMapper.toDto(location));
    }

    @GetMapping("/{locationId}")
    public LocationDto getById(@PathVariable UUID locationId) {
        return locationService.getDtoById(locationId);
    }
}
