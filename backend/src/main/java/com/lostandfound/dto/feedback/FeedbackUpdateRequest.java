package com.lostandfound.dto.feedback;

import com.lostandfound.entity.enums.FeedbackStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FeedbackUpdateRequest {
    @NotNull
    private FeedbackStatus status;

    @NotBlank
    @Size(max = 10000)
    private String adminResponse;
}