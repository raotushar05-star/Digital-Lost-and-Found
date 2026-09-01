package com.lostandfound.controller;

import com.lostandfound.dto.foundreport.FoundReportCreateRequest;
import com.lostandfound.dto.foundreport.FoundReportCreateResponse;
import com.lostandfound.dto.foundreport.FoundReportDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.FoundReportService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/found-reports")
@RequiredArgsConstructor
public class FoundReportController {

    private final FoundReportService foundReportService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FoundReportCreateResponse> create(@Valid @RequestBody FoundReportCreateRequest request) {
        FoundReportCreateResponse response = foundReportService.create(userService.getCurrentUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{foundReportId}")
    public FoundReportDto getById(@PathVariable UUID foundReportId) {
        return foundReportService.getById(foundReportId, SecurityUtils.getCurrentPrincipal());
    }
}
