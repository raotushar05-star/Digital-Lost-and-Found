package com.lostandfound.dto.category;

import lombok.*;

import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryDto {
    private UUID categoryId;
    private String categoryName;
    private String description;
    private Boolean isActive;
}
