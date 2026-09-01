package com.lostandfound.dto.file;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PhotoUploadResponse {
    private UUID photoId;
    private String fileUrl;
    private String message;
}
