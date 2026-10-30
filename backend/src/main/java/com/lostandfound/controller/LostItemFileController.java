package com.lostandfound.controller;

import com.lostandfound.entity.ItemPhoto;
import com.lostandfound.entity.LostItem;
import com.lostandfound.exception.ForbiddenException;
import com.lostandfound.exception.ResourceNotFoundException;
import com.lostandfound.repository.ItemPhotoRepository;
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

/** Serves lost-report photos only to the owner, or to police of the report's station. */
@RestController
@RequiredArgsConstructor
public class LostItemFileController {

    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9-]{36}\\.[a-z0-9]{2,5}$");

    private final ItemPhotoRepository photoRepository;

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @GetMapping("/api/v1/files/lost-items/{filename:.+}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> get(@PathVariable String filename) throws IOException {
        if (!SAFE_NAME.matcher(filename).matches()) {
            throw new ResourceNotFoundException("File not found");
        }
        ItemPhoto photo = photoRepository.findFirstByFileUrlEndingWith("/lost-items/" + filename)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        LostItem item = photo.getLostItem();
        if (item == null) {
            throw new ResourceNotFoundException("File not found");
        }

        UserPrincipal principal = SecurityUtils.getCurrentPrincipal();
        boolean owner = item.getOwner().getUserId().equals(principal.getUserId());
        if (!owner && !StationAccess.isSystemAdmin(principal)) {
            if (!StationAccess.isPoliceStaff(principal)) {
                throw new ForbiddenException("You do not have permission to view this file");
            }
            if (item.getAssignedStation() != null) {
                StationAccess.assertOwnStation(principal, item.getAssignedStation());
            } else if (principal.getStationId() == null) {
                throw new ForbiddenException("Your police account is not assigned to a station");
            }
        }

        Path file = Paths.get(uploadDir, "lost-items", filename).toAbsolutePath().normalize();
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