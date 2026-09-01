package com.lostandfound.controller;

import com.lostandfound.dto.claim.ClaimSummaryDto;
import com.lostandfound.dto.file.PhotoUploadResponse;
import com.lostandfound.dto.founditem.FoundItemAdminDto;
import com.lostandfound.dto.founditem.FoundItemIntakeRequest;
import com.lostandfound.dto.founditem.FoundItemIntakeResponse;
import com.lostandfound.dto.verification.VerificationResponse;
import com.lostandfound.dto.verification.VerifyFoundItemRequest;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.ClaimService;
import com.lostandfound.service.FoundItemService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/police")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN')")
public class PoliceFoundItemController {

    private final FoundItemService foundItemService;
    private final ClaimService claimService;
    private final UserService userService;

    @PostMapping("/found-items")
    public ResponseEntity<FoundItemIntakeResponse> intake(@Valid @RequestBody FoundItemIntakeRequest request) {
        FoundItemIntakeResponse response = foundItemService.intake(userService.getCurrentUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/found-items/{foundItemId}")
    public FoundItemAdminDto getDetail(@PathVariable UUID foundItemId) {
        return foundItemService.getAdminDetail(foundItemId, SecurityUtils.getCurrentPrincipal());
    }

    @GetMapping("/stations/{stationId}/found-items")
    public List<FoundItemAdminDto> getStationInventory(@PathVariable UUID stationId) {
        return foundItemService.getStationInventory(stationId, SecurityUtils.getCurrentPrincipal());
    }

    @PostMapping(value = "/found-items/{foundItemId}/photos", consumes = "multipart/form-data")
    public PhotoUploadResponse uploadPhoto(@PathVariable UUID foundItemId,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "visibility", required = false) String visibility) {
        return foundItemService.uploadPhoto(foundItemId, file, visibility, userService.getCurrentUser());
    }

    @PostMapping("/found-items/{foundItemId}/verify")
    public VerificationResponse verify(@PathVariable UUID foundItemId, @Valid @RequestBody VerifyFoundItemRequest request) {
        return foundItemService.verify(foundItemId, userService.getCurrentUser(), request);
    }

    @GetMapping("/found-items/{foundItemId}/claims")
    public List<ClaimSummaryDto> getClaims(@PathVariable UUID foundItemId) {
        return claimService.getClaimsForFoundItem(foundItemId, SecurityUtils.getCurrentPrincipal());
    }
}
