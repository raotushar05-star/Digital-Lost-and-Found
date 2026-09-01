package com.lostandfound.controller;

import com.lostandfound.dto.station.StationDto;
import com.lostandfound.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN')")
public class StationController {

    private final StationService stationService;

    @GetMapping
    public List<StationDto> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{stationId}")
    public StationDto getStation(@PathVariable UUID stationId) {
        return stationService.getById(stationId);
    }
}
