package com.lostandfound.controller;

import com.lostandfound.dto.handover.HandoverRequest;
import com.lostandfound.dto.handover.HandoverResponse;
import com.lostandfound.service.HandoverService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/police/claims")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN')")
public class PoliceHandoverController {

    private final HandoverService handoverService;
    private final UserService userService;

    @PostMapping("/{claimId}/handover")
    public ResponseEntity<HandoverResponse> recordHandover(@PathVariable UUID claimId, @Valid @RequestBody HandoverRequest request) {
        HandoverResponse response = handoverService.recordHandover(claimId, userService.getCurrentUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
