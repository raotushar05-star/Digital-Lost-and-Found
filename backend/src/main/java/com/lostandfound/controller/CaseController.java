package com.lostandfound.controller;

import com.lostandfound.dto.casedto.CaseDto;
import com.lostandfound.dto.casedto.CaseFileDto;
import com.lostandfound.dto.casedto.CaseHistoryResponse;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.CaseFileService;
import com.lostandfound.service.CaseReportExportService;
import com.lostandfound.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final CaseFileService caseFileService;
    private final CaseReportExportService exportService;

    @GetMapping("/{caseId}")
    public CaseDto getById(@PathVariable UUID caseId) {
        return caseService.getDto(caseId, SecurityUtils.getCurrentPrincipal());
    }

    @GetMapping("/{caseId}/history")
    public CaseHistoryResponse getHistory(@PathVariable UUID caseId) {
        return caseService.getHistory(caseId, SecurityUtils.getCurrentPrincipal());
    }

    @GetMapping("/{caseId}/file")
    public CaseFileDto getCaseFile(@PathVariable UUID caseId) {
        return caseFileService.getById(caseId, SecurityUtils.getCurrentPrincipal());
    }

    @GetMapping(value = "/{caseId}/file/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadCaseFilePdf(@PathVariable UUID caseId) {
        return PoliceReportsController.pdfResponse(
                exportService.exportCase(caseId, SecurityUtils.getCurrentPrincipal()));
    }
}