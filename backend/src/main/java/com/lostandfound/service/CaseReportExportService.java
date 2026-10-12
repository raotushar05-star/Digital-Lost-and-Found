package com.lostandfound.service;

import com.lostandfound.dto.casedto.CaseFileDto;
import com.lostandfound.dto.reports.ReportsSummaryDto;
import com.lostandfound.dto.reports.StationReportDto;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates PDF exports. Authorization and station scoping are delegated to
 * CaseFileService, so the PDF can never contain more than the JSON endpoints already expose.
 */
@Service
@RequiredArgsConstructor
public class CaseReportExportService {

    private final CaseFileService caseFileService;
    private final ReportsService reportsService;
    private final CaseFilePdfService pdfService;

    public record PdfFile(String filename, byte[] content) {}

    /** Individual case report. Citizens (owner/finder) get the non-restricted view. */
    public PdfFile exportCase(UUID caseId, UserPrincipal principal) {
        CaseFileDto caseFile = caseFileService.getById(caseId, principal);
        return new PdfFile("findline-case-" + slug(caseFile.getCaseNumber()) + ".pdf",
                pdfService.renderSingle(caseFile));
    }

    /** Bulk report: every case visible to this police user within the date range. */
    public PdfFile exportAllCases(UserPrincipal principal, LocalDate dateFrom, LocalDate dateTo) {
        List<CaseFileDto> caseFiles = caseFileService.listForPolice(principal, dateFrom, dateTo);

        boolean systemAdmin = "SYSTEM_ADMIN".equals(principal.getRole());
        boolean stationAdmin = "POLICE_ADMIN".equals(principal.getRole());

        // Officers/station admins: summary scoped to their own station. System admin: all stations.
        UUID scope = systemAdmin ? null : principal.getStationId();
        ReportsSummaryDto summary = reportsService.getSummary(scope, dateFrom, dateTo);
        List<StationReportDto> stationReports = (systemAdmin || stationAdmin)
                ? reportsService.getStationReports(dateFrom, dateTo)
                : List.of();

        String period = describePeriod(dateFrom, dateTo);
        byte[] pdf = pdfService.renderBundle(caseFiles, summary, stationReports, period);
        String filename = "findline-all-cases-"
                + (dateFrom == null ? "start" : dateFrom) + "-to-"
                + (dateTo == null ? "present" : dateTo) + ".pdf";
        return new PdfFile(filename, pdf);
    }

    private static String describePeriod(LocalDate from, LocalDate to) {
        if (from == null && to == null) return "All dates";
        return (from == null ? "Start" : from.toString()) + " to " + (to == null ? "Present" : to.toString());
    }

    private static String slug(String value) {
        String base = value == null || value.isBlank() ? "case" : value;
        return base.replaceAll("[^\\w.-]+", "-");
    }
}