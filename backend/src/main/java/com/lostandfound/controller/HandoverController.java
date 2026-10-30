package com.lostandfound.controller;

import com.lostandfound.dto.handover.HandoverCodeResponse;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.HandoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class HandoverController {

    private final HandoverService handoverService;

    @PostMapping("/api/v1/claims/{claimId}/handover-code")
    @PreAuthorize("hasRole('USER')")
    public HandoverCodeResponse issueCode(@PathVariable UUID claimId) {
        return handoverService.issueCode(claimId, SecurityUtils.getCurrentPrincipal());
    }

    @GetMapping(value = "/api/v1/claims/{claimId}/handover-receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> receipt(@PathVariable UUID claimId) {
        return PoliceReportsController.pdfResponse(
                handoverService.receipt(claimId, SecurityUtils.getCurrentPrincipal()));
    }
}