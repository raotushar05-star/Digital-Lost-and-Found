package com.lostandfound.dto.feedback;

import com.lostandfound.entity.enums.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FeedbackCreateRequest {
    @NotNull
    private FeedbackType feedbackType;

    @NotBlank
    @Size(max = 150)
    private String subject;

    @NotBlank
    @Size(max = 10000)
    private String message;
}