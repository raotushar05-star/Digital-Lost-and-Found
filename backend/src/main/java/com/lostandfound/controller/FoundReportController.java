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
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/found-reports")
@RequiredArgsConstructor
public class FoundReportController {

    private final FoundReportService foundReportService;
    private final UserService userService;

    /**
     * Multipart: the "request" part carries the report fields as JSON, the
     * "photo" part is a required image proving what was found. Required
     * because it strengthens the proof-of-discovery trail captured before
     * police ever see the item.
     */
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FoundReportCreateResponse> create(
            @Valid @RequestPart("request") FoundReportCreateRequest request,
            @RequestPart("photo") MultipartFile photo) {
        FoundReportCreateResponse response = foundReportService.create(userService.getCurrentUser(), request, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{foundReportId}")
    public FoundReportDto getById(@PathVariable UUID foundReportId) {
        return foundReportService.getById(foundReportId, SecurityUtils.getCurrentPrincipal());
    }
}