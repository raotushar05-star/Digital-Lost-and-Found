package com.lostandfound.dto.evidence;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvidenceCreateRequest {

    @NotBlank(message = "Evidence type is required")
    private String evidenceType;

    private String description;
}
