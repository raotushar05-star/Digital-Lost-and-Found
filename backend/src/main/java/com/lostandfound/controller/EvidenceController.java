package com.lostandfound.controller;

import com.lostandfound.dto.evidence.EvidenceCreateRequest;
import com.lostandfound.dto.evidence.EvidenceDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.EvidenceService;
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
@RequestMapping("/api/v1/claims/{claimId}/evidence")
@RequiredArgsConstructor
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final UserService userService;

    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EvidenceDto> addEvidence(@PathVariable UUID claimId,
                                                     @RequestParam("evidenceType") String evidenceType,
                                                     @RequestParam(value = "description", required = false) String description,
                                                     @RequestParam(value = "file", required = false) MultipartFile file) {
        EvidenceCreateRequest request = EvidenceCreateRequest.builder()
                .evidenceType(evidenceType)
                .description(description)
                .build();
        EvidenceDto dto = evidenceService.addEvidence(claimId, userService.getCurrentUser(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public List<EvidenceDto> getEvidence(@PathVariable UUID claimId) {
        return evidenceService.getEvidence(claimId, SecurityUtils.getCurrentPrincipal());
    }
}
