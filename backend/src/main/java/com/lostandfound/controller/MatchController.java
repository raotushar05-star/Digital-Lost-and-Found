package com.lostandfound.controller;

import com.lostandfound.dto.match.GenerateMatchesResponse;
import com.lostandfound.dto.match.MatchDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.LostItemService;
import com.lostandfound.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Smart Matching Module. Matches are always POTENTIAL - ownership is only
 * confirmed through a claim plus an explicit police verification decision.
 */
@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchingService matchingService;
    private final LostItemService lostItemService;

    @GetMapping("/api/v1/matches/my")
    @PreAuthorize("hasRole('USER')")
    public List<MatchDto> getMyMatches() {
        return matchingService.getMyMatches(SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/api/v1/lost-items/{lostItemId}/matches")
    public List<MatchDto> getMatchesForLostItem(@PathVariable UUID lostItemId) {
        return matchingService.getMatchesForLostItem(lostItemId, SecurityUtils.getCurrentPrincipal());
    }

    @PostMapping("/api/v1/matches/generate")
    @PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN')")
    public GenerateMatchesResponse generate(@RequestParam(required = false) UUID lostItemId) {
        int created;
        if (lostItemId != null) {
            created = matchingService.generateForLostItem(lostItemService.getEntityById(lostItemId));
        } else {
            created = matchingService.generateGlobal();
        }
        return GenerateMatchesResponse.builder().matchesGenerated(created).build();
    }
}