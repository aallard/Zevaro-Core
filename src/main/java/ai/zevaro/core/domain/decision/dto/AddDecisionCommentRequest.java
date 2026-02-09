package ai.zevaro.core.domain.decision.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AddDecisionCommentRequest(
        @NotBlank String body,
        UUID parentCommentId
) {}
