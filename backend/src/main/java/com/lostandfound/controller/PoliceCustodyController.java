package com.lostandfound.controller;

import com.lostandfound.dto.common.MessageResponse;
import com.lostandfound.dto.custody.DisposeRequest;
import com.lostandfound.dto.custody.TransferDto;
import com.lostandfound.dto.custody.TransferRequest;
import com.lostandfound.service.CustodyTransferService;
import com.lostandfound.service.DisposalService;
import com.lostandfound.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/police/found-items/{foundItemId}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICE_OFFICER','POLICE_ADMIN')")
public class PoliceCustodyController {

    private final CustodyTransferService transferService;
    private final DisposalService disposalService;
    private final UserService userService;

    @PostMapping("/transfer")
    public TransferDto transfer(@PathVariable UUID foundItemId, @Valid @RequestBody TransferRequest request) {
        return transferService.transfer(foundItemId, userService.getCurrentUser(), request);
    }

    @GetMapping("/transfers")
    public List<TransferDto> transfers(@PathVariable UUID foundItemId) {
        return transferService.history(foundItemId, userService.getCurrentUser());
    }

    @PostMapping("/dispose")
    public MessageResponse dispose(@PathVariable UUID foundItemId, @Valid @RequestBody DisposeRequest request) {
        disposalService.dispose(foundItemId, userService.getCurrentUser(), request);
        return MessageResponse.builder().message("Item marked as disposed").build();
    }
}