package com.lostandfound.controller;

import com.lostandfound.dto.file.PhotoUploadResponse;
import com.lostandfound.dto.common.MessageResponse;
import com.lostandfound.dto.lostitem.*;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.LostItemService;
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
@RequestMapping("/api/v1/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LostItemCreateResponse> create(@Valid @RequestBody LostItemCreateRequest request) {
        UUID ownerId = SecurityUtils.getCurrentUserId();
        LostItemCreateResponse response = lostItemService.create(ownerId, request, userService.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{lostItemId}")
    public LostItemDetailDto getDetail(@PathVariable UUID lostItemId) {
        return lostItemService.getDetail(lostItemId, SecurityUtils.getCurrentPrincipal());
    }

    @PutMapping("/{lostItemId}")
    public LostItemDetailDto update(@PathVariable UUID lostItemId, @Valid @RequestBody LostItemUpdateRequest request) {
        return lostItemService.update(lostItemId, request, SecurityUtils.getCurrentPrincipal());
    }

    @DeleteMapping("/{lostItemId}")
    public MessageResponse withdraw(@PathVariable UUID lostItemId) {
        lostItemService.withdraw(lostItemId, SecurityUtils.getCurrentPrincipal());
        return MessageResponse.builder().message("Lost-item report withdrawn").build();
    }

    @PostMapping(value = "/{lostItemId}/photos", consumes = "multipart/form-data")
    public PhotoUploadResponse uploadPhoto(@PathVariable UUID lostItemId, @RequestParam("file") MultipartFile file) {
        return lostItemService.uploadPhoto(lostItemId, file, SecurityUtils.getCurrentPrincipal());
    }
}
