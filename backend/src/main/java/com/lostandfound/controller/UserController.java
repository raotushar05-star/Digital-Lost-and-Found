package com.lostandfound.controller;

import com.lostandfound.dto.casedto.CaseDto;
import com.lostandfound.dto.claim.ClaimSummaryDto;
import com.lostandfound.dto.foundreport.FoundReportDto;
import com.lostandfound.dto.lostitem.LostItemSummaryDto;
import com.lostandfound.dto.user.UpdateProfileRequest;
import com.lostandfound.dto.user.UserProfileDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public UserProfileDto getProfile() {
        return userService.getProfile(SecurityUtils.getCurrentUserId());
    }

    @PutMapping
    public UserProfileDto updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(SecurityUtils.getCurrentUserId(), request);
    }

    @GetMapping("/lost-items")
    public List<LostItemSummaryDto> getMyLostItems() {
        return userService.getMyLostItems(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/found-reports")
    public List<FoundReportDto> getMyFoundReports() {
        return userService.getMyFoundReports(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/claims")
    public List<ClaimSummaryDto> getMyClaims() {
        return userService.getMyClaims(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/cases")
    public List<CaseDto> getMyCases() {
        return userService.getMyCases(SecurityUtils.getCurrentUserId());
    }
}
