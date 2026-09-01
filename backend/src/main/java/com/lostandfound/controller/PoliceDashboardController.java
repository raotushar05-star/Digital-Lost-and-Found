package com.lostandfound.controller;

import com.lostandfound.dto.dashboard.PoliceDashboardDto;
import com.lostandfound.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/police/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN')")
public class PoliceDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public PoliceDashboardDto getDashboard() {
        return dashboardService.getPoliceDashboard();
    }
}
