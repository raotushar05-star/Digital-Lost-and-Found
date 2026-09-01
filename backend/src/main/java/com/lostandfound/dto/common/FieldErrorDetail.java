package com.lostandfound.dto.common;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FieldErrorDetail {
    private String field;
    private String message;
}
