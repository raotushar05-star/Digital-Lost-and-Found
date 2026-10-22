package com.lostandfound.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SetActiveRequest {
    @NotNull(message = "active is required")
    private Boolean active;
}