package ai.zevaro.core.domain.comment;

import ai.zevaro.core.domain.comment.dto.CommentResponse;
import ai.zevaro.core.domain.comment.dto.CreateCommentRequest;
import ai.zevaro.core.domain.comment.dto.UpdateCommentRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommentMapper {

    public Comment toEntity(CreateCommentRequest request, UUID tenantId, UUID userId) {
        Comment comment = new Comment();
        comment.setTenantId(tenantId);
        comment.setParentType(request.parentType());
        comment.setParentId(request.parentId());
        comment.setAuthorId(userId);
        comment.setBody(request.body());
        comment.setParentCommentId(request.parentCommentId());
        comment.setEdited(false);
        return comment;
    }

    public CommentResponse toResponse(Comment comment, String authorName, int replyCount) {
        return new CommentResponse(
                comment.getId(),
                comment.getParentType(),
                comment.getParentId(),
                comment.getAuthorId(),
                authorName,
                comment.getBody(),
                comment.getParentCommentId(),
                comment.isEdited(),
                replyCount,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public void applyUpdate(Comment existing, UpdateCommentRequest request) {
        existing.setBody(request.body());
        existing.setEdited(true);
    }
}
