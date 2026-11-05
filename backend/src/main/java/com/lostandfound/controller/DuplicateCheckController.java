package com.lostandfound.controller;

import com.lostandfound.dto.duplicate.DuplicateCheckRequest;
import com.lostandfound.dto.duplicate.DuplicateHitDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.DuplicateCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/duplicate-check")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class DuplicateCheckController {

    private final DuplicateCheckService service;

    @PostMapping("/lost")
    public List<DuplicateHitDto> lost(@Valid @RequestBody DuplicateCheckRequest request) {
        return service.checkLost(SecurityUtils.getCurrentUserId(), request);
    }

    @PostMapping("/found")
    public List<DuplicateHitDto> found(@Valid @RequestBody DuplicateCheckRequest request) {
        return service.checkFound(SecurityUtils.getCurrentUserId(), request);
    }
}