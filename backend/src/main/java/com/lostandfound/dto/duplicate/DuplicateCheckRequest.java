package com.lostandfound.dto.duplicate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DuplicateCheckRequest(
        @NotNull UUID categoryId,
        @NotNull LocalDate date,
        @NotNull BigDecimal latitude,
        @NotNull BigDecimal longitude,
        @Size(max = 1000) String description,
        @Size(max = 100) String brand) {
}