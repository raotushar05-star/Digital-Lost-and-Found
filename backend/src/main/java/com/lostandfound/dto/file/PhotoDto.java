package com.lostandfound.dto.file;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PhotoDto {
    private UUID photoId;
    private String fileUrl;
    private Boolean isPrimary;
    private String visibility;
    private LocalDateTime createdAt;
}
