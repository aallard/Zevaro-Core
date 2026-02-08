package ai.zevaro.core.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @NotBlank String body
) {}
