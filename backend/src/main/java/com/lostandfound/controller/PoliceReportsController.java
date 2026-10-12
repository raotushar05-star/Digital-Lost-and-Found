package com.lostandfound.controller;

import com.lostandfound.dto.casedto.CaseFileDto;
import com.lostandfound.dto.reports.ReportsSummaryDto;
import com.lostandfound.dto.reports.StationReportDto;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.CaseFileService;
import com.lostandfound.service.CaseReportExportService;
import com.lostandfound.service.CaseReportExportService.PdfFile;
import com.lostandfound.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final CaseFileService caseFileService;
    private final CaseReportExportService exportService;

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

    @GetMapping("/case-files")
    @PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN')")
    public List<CaseFileDto> getCaseFiles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return caseFileService.listForPolice(SecurityUtils.getCurrentPrincipal(), dateFrom, dateTo);
    }

    @GetMapping("/case-files/{caseId}")
    @PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN')")
    public CaseFileDto getCaseFile(@PathVariable UUID caseId) {
        return caseFileService.getById(caseId, SecurityUtils.getCurrentPrincipal());
    }

    /** Individual case report as PDF. */
    @GetMapping(value = "/case-files/{caseId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<byte[]> downloadCaseFilePdf(@PathVariable UUID caseId) {
        return pdfResponse(exportService.exportCase(caseId, SecurityUtils.getCurrentPrincipal()));
    }

    /** Bulk report: all detailed case records in range, as one PDF. */
    @GetMapping(value = "/case-files/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN','SYSTEM_ADMIN')")
    public ResponseEntity<byte[]> downloadAllCaseFilesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return pdfResponse(exportService.exportAllCases(SecurityUtils.getCurrentPrincipal(), dateFrom, dateTo));
    }

    static ResponseEntity<byte[]> pdfResponse(PdfFile file) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.filename()).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentLength(file.content().length)
                .body(file.content());
    }
}