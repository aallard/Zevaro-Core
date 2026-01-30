package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.UUID;

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
