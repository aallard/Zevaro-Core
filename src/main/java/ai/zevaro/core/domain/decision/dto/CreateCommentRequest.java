package ai.zevaro.core.domain.decision.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCommentRequest(
        @NotBlank String content,
        String optionId,
        UUID parentId
) {}
