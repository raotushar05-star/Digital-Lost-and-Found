package com.lostandfound.controller;

import com.lostandfound.entity.Claim;
import com.lostandfound.entity.ClaimEvidence;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.ClaimEvidenceRepository;
import com.lostandfound.security.SecurityUtils;
import com.lostandfound.security.StationAccess;
import com.lostandfound.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/** Serves claim evidence only to the claimant, or police of the item's station. */
@RestController
@RequiredArgsConstructor
public class EvidenceFileController {

    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9-]{36}\\.[a-z0-9]{2,5}$");

    private final ClaimEvidenceRepository evidenceRepository;

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @GetMapping("/api/v1/files/evidence/{filename:.+}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> get(@PathVariable String filename) throws IOException {
        if (!SAFE_NAME.matcher(filename).matches()) {
            throw new ResourceNotFoundException("File not found");
        }
        ClaimEvidence evidence = evidenceRepository.findFirstByFileUrlEndingWith("/evidence/" + filename)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        UserPrincipal principal = SecurityUtils.getCurrentPrincipal();
        Claim claim = evidence.getClaim();
        boolean isClaimant = claim.getClaimant().getUserId().equals(principal.getUserId());
        if (!isClaimant) {
            if (!StationAccess.isSystemAdmin(principal) && !StationAccess.isPoliceStaff(principal)) {
                throw new ForbiddenException("You do not have permission to view this file");
            }
            StationAccess.assertOwnStation(principal, claim.getFoundItem().getStation());
        }

        Path file = Paths.get(uploadDir, "evidence", filename).toAbsolutePath().normalize();
        if (!Files.isReadable(file)) {
            throw new ResourceNotFoundException("File not found");
        }
        String type = Files.probeContentType(file);
        MediaType mediaType = type != null ? MediaType.parseMediaType(type) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(new FileSystemResource(file));
    }
}