package com.lostandfound.controller;

import com.lostandfound.dto.lostitem.LostItemDetailDto;
import com.lostandfound.dto.lostitem.LostItemSummaryDto;
import com.lostandfound.entity.enums.LostItemStatus;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.LostItemService;
import com.lostandfound.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/police/lost-reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN')")
public class PoliceLostItemController {

    private final LostItemService lostItemService;
    private final UserService userService;

    @GetMapping
    public List<LostItemSummaryDto> getLostReports(
            @RequestParam(required = false) LostItemStatus status) {
        return lostItemService.getPoliceLostReports(SecurityUtils.getCurrentPrincipal(), status);
    }

    @GetMapping("/{lostItemId}")
    public LostItemDetailDto getLostReport(@PathVariable UUID lostItemId) {
        return lostItemService.getPoliceLostReport(lostItemId, SecurityUtils.getCurrentPrincipal());
    }

    @PatchMapping("/{lostItemId}/receive")
    public LostItemDetailDto acknowledgeReceipt(@PathVariable UUID lostItemId) {
        return lostItemService.acknowledgePoliceReceipt(lostItemId, userService.getCurrentUser());
    }
}