package com.lostandfound.controller;

import com.lostandfound.dto.dispute.DisputeCreateRequest;
import com.lostandfound.dto.dispute.DisputeDto;
import com.lostandfound.dto.dispute.DisputeUpdateRequest;
import com.lostandfound.service.DisputeService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/police")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN')")
public class PoliceDisputeController {

    private final DisputeService disputeService;
    private final UserService userService;

    @GetMapping("/found-items/{foundItemId}/disputes")
    public List<DisputeDto> getDisputes(@PathVariable UUID foundItemId) {
        return disputeService.getDisputesForFoundItem(foundItemId);
    }

    @PostMapping("/found-items/{foundItemId}/disputes")
    public ResponseEntity<DisputeDto> create(@PathVariable UUID foundItemId, @Valid @RequestBody DisputeCreateRequest request) {
        DisputeDto dto = disputeService.create(foundItemId, userService.getCurrentUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/disputes/{disputeId}")
    public DisputeDto update(@PathVariable UUID disputeId, @Valid @RequestBody DisputeUpdateRequest request) {
        return disputeService.update(disputeId, userService.getCurrentUser(), request);
    }
}
