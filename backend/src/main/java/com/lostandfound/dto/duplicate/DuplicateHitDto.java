package com.lostandfound.dto.duplicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DuplicateHitDto(UUID id, String kind, String category, String description,
                              LocalDate date, String city, String status, LocalDateTime createdAt) {
}