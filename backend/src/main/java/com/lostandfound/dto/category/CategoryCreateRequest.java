package com.lostandfound.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private String description;
}
