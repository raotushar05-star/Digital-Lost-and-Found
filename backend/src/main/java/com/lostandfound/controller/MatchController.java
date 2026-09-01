package com.lostandfound.controller;

import com.lostandfound.dto.match.GenerateMatchesResponse;
import com.lostandfound.dto.match.MatchDto;
import com.lostandfound.entity.LostItem;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.mapper.MatchMapper;
import com.lostandfound.repository.MatchRepository;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.security.UserPrincipal;
import com.lostandfound.service.LostItemService;
import com.lostandfound.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Smart Matching Module. Matches are always POTENTIAL - ownership is only
 * confirmed through a claim plus an explicit police verification decision.
 */
@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final LostItemService lostItemService;
    private final MatchingService matchingService;

    @GetMapping("/api/v1/matches/my")
    @PreAuthorize("hasRole('USER')")
    public List<MatchDto> getMyMatches() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return matchRepository.findByLostItem_Owner_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(matchMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v1/lost-items/{lostItemId}/matches")
    public List<MatchDto> getMatchesForLostItem(@PathVariable UUID lostItemId) {
        UserPrincipal principal = SecurityUtils.getCurrentPrincipal();
        LostItem lostItem = lostItemService.getEntityById(lostItemId);
        boolean isOwner = lostItem.getOwner().getUserId().equals(principal.getUserId());
        boolean isPolice = "POLICE_OFFICER".equals(principal.getRole()) || "POLICE_ADMIN".equals(principal.getRole()) || "SYSTEM_ADMIN".equals(principal.getRole());
        if (!isOwner && !isPolice) {
            throw new ForbiddenException("You do not have permission to view matches for this item");
        }
        return matchRepository.findByLostItem_LostItemIdOrderByMatchScoreDesc(lostItemId).stream()
                .map(matchMapper::toDto)
                .collect(Collectors.toList());
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
