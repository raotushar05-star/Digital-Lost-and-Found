package com.lostandfound.controller;

import com.lostandfound.dto.verification.VerificationResponse;
import com.lostandfound.dto.verification.VerifyClaimRequest;
import com.lostandfound.service.OwnershipVerificationService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/police/claims")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN')")
public class PoliceOwnershipVerificationController {

    private final OwnershipVerificationService ownershipVerificationService;
    private final UserService userService;

    @PostMapping("/{claimId}/verify")
    public VerificationResponse verify(@PathVariable UUID claimId, @Valid @RequestBody VerifyClaimRequest request) {
        return ownershipVerificationService.verify(claimId, userService.getCurrentUser(), request);
    }
}
