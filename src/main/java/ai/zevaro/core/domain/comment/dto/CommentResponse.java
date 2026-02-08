package ai.zevaro.core.domain.comment.dto;

import ai.zevaro.core.domain.comment.CommentParentType;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        CommentParentType parentType,
        UUID parentId,
        UUID authorId,
        String authorName,
        String body,
        UUID parentCommentId,
        boolean edited,
        int replyCount,
        Instant createdAt,
        Instant updatedAt
) {}
