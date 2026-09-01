package com.lostandfound.controller;

import com.lostandfound.dto.station.StationCreateRequest;
import com.lostandfound.dto.station.StationDto;
import com.lostandfound.dto.station.StationUpdateRequest;
import com.lostandfound.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/stations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminStationController {

    private final StationService stationService;

    @PostMapping
    public ResponseEntity<StationDto> create(@Valid @RequestBody StationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stationService.create(request));
    }

    @PutMapping("/{stationId}")
    public StationDto update(@PathVariable UUID stationId, @Valid @RequestBody StationUpdateRequest request) {
        return stationService.update(stationId, request);
    }
}
