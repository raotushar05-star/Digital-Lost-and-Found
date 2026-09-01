package com.lostandfound.controller;

import com.lostandfound.dto.claim.ClaimCreateRequest;
import com.lostandfound.dto.claim.ClaimCreateResponse;
import com.lostandfound.dto.claim.ClaimDetailDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.ClaimService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final UserService userService;

    @PostMapping("/api/v1/found-items/{foundItemId}/claims")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ClaimCreateResponse> create(@PathVariable UUID foundItemId, @Valid @RequestBody ClaimCreateRequest request) {
        ClaimCreateResponse response = claimService.create(foundItemId, userService.getCurrentUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/claims/{claimId}")
    public ClaimDetailDto getById(@PathVariable UUID claimId) {
        return claimService.getById(claimId, SecurityUtils.getCurrentPrincipal());
    }
}
