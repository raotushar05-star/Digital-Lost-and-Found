package com.lostandfound.controller;

import com.lostandfound.dto.category.CategoryCreateRequest;
import com.lostandfound.dto.category.CategoryDto;
import com.lostandfound.dto.category.CategoryUpdateRequest;
import com.lostandfound.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{categoryId}")
    public CategoryDto update(@PathVariable UUID categoryId, @Valid @RequestBody CategoryUpdateRequest request) {
        return categoryService.update(categoryId, request);
    }
}
