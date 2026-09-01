package com.lostandfound.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryUpdateRequest {

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private String description;
    private Boolean isActive;
}
