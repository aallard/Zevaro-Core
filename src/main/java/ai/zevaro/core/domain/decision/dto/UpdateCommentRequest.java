package ai.zevaro.core.domain.decision.dto;

import jakarta.validation.constraints.NotBlank;

// Replaced by polymorphic Comment entity in domain/comment/. Will be removed after data migration in ZC-064.
@Deprecated
public record UpdateCommentRequest(
        @NotBlank String content
) {}
