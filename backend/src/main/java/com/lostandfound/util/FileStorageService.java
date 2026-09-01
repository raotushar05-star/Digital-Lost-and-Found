package com.lostandfound.util;

import com.lostandfound.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "pdf");
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Value("${app.file-storage.base-url}")
    private String baseUrl;

    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Uploaded file exceeds the maximum allowed size of 10MB");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = "";
        int dot = original.lastIndexOf(".");
        if (dot >= 0) {
            extension = original.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type. Allowed types: " + ALLOWED_EXTENSIONS);
        }
        try {
            Path folder = Paths.get(uploadDir, subFolder);
            Files.createDirectories(folder);
            String filename = UUID.randomUUID() + "." + extension;
            Path target = folder.resolve(filename);
            Files.copy(file.getInputStream(), target);
            return baseUrl + "/" + subFolder + "/" + filename;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store uploaded file: " + ex.getMessage());
        }
    }
}
