package com.lostandfound.controller;

import com.lostandfound.dto.casedto.CaseDto;
import com.lostandfound.dto.casedto.CaseHistoryResponse;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @GetMapping("/{caseId}")
    public CaseDto getById(@PathVariable UUID caseId) {
        return caseService.getDto(caseId, SecurityUtils.getCurrentPrincipal());
    }

    @GetMapping("/{caseId}/history")
    public CaseHistoryResponse getHistory(@PathVariable UUID caseId) {
        return caseService.getHistory(caseId, SecurityUtils.getCurrentPrincipal());
    }
}
