package com.lostandfound.controller;

import com.lostandfound.dto.reports.ReportsSummaryDto;
import com.lostandfound.dto.reports.StationReportDto;
import com.lostandfound.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/police/reports")
@RequiredArgsConstructor
public class PoliceReportsController {

    private final ReportsService reportsService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN')")
    public ReportsSummaryDto getSummary(
            @RequestParam(required = false) UUID stationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return reportsService.getSummary(stationId, dateFrom, dateTo);
    }

    @GetMapping("/stations")
    @PreAuthorize("hasAnyRole('POLICE_ADMIN','SYSTEM_ADMIN')")
    public List<StationReportDto> getStationReports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return reportsService.getStationReports(dateFrom, dateTo);
    }
}
