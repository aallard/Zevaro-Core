package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.UUID;

// Replaced by polymorphic Comment entity in domain/comment/. Will be removed after data migration in ZC-064.
@Deprecated
public record CommentResponse(
        UUID id,
        UserSummary author,
        String content,
        String optionId,
        UUID parentId,
        boolean edited,
        Instant createdAt,
        Instant updatedAt
) {}
