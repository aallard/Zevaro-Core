package ai.zevaro.core.domain.comment.dto;

import ai.zevaro.core.domain.comment.CommentParentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCommentRequest(
        @NotNull CommentParentType parentType,
        @NotNull UUID parentId,
        @NotBlank String body,
        UUID parentCommentId
) {}
