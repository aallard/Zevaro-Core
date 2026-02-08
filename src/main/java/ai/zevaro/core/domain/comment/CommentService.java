package ai.zevaro.core.domain.comment;

import ai.zevaro.core.domain.audit.AuditAction;
import ai.zevaro.core.domain.audit.AuditLogBuilder;
import ai.zevaro.core.domain.audit.AuditService;
import ai.zevaro.core.domain.comment.dto.CommentResponse;
import ai.zevaro.core.domain.comment.dto.CreateCommentRequest;
import ai.zevaro.core.domain.comment.dto.UpdateCommentRequest;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final AuditService auditService;

    @Transactional
    public CommentResponse create(CreateCommentRequest request, UUID tenantId, UUID userId) {
        if (request.parentCommentId() != null) {
            Comment parentComment = commentRepository.findByIdAndTenantId(request.parentCommentId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.parentCommentId()));
            if (parentComment.getParentType() != request.parentType()
                    || !parentComment.getParentId().equals(request.parentId())) {
                throw new IllegalStateException("Parent comment must belong to the same entity");
            }
        }

        Comment comment = commentMapper.toEntity(request, tenantId, userId);
        comment = commentRepository.save(comment);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.CREATE)
                .entity("Comment", comment.getId(), null)
                .description("Comment created on " + request.parentType() + " " + request.parentId()));

        return toResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getByParent(CommentParentType parentType, UUID parentId, UUID tenantId) {
        return commentRepository.findByTenantIdAndParentTypeAndParentIdOrderByCreatedAtAsc(tenantId, parentType, parentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getByParentPaged(CommentParentType parentType, UUID parentId, UUID tenantId, Pageable pageable) {
        return commentRepository.findByTenantIdAndParentTypeAndParentId(tenantId, parentType, parentId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CommentResponse update(UUID id, UpdateCommentRequest request, UUID tenantId, UUID userId) {
        Comment comment = commentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getAuthorId().equals(userId)) {
            throw new IllegalStateException("Only the author can edit their comment");
        }

        commentMapper.applyUpdate(comment, request);
        comment = commentRepository.save(comment);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.UPDATE)
                .entity("Comment", comment.getId(), null)
                .description("Comment updated"));

        return toResponse(comment);
    }

    @Transactional
    public void delete(UUID id, UUID tenantId, UUID userId) {
        Comment comment = commentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getAuthorId().equals(userId)) {
            throw new IllegalStateException("Only the author can delete their comment");
        }

        commentRepository.delete(comment);

        auditService.log(AuditLogBuilder.create()
                .tenant(tenantId)
                .actor(userId, null, null)
                .action(AuditAction.DELETE)
                .entity("Comment", id, null)
                .description("Comment deleted"));
    }

    @Transactional(readOnly = true)
    public long countByParent(CommentParentType parentType, UUID parentId, UUID tenantId) {
        return commentRepository.countByTenantIdAndParentTypeAndParentId(tenantId, parentType, parentId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(UUID commentId, UUID tenantId) {
        commentRepository.findByIdAndTenantId(commentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        return commentRepository.findByParentCommentId(commentId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment comment) {
        String authorName = userRepository.findById(comment.getAuthorId())
                .map(User::getFullName)
                .orElse(null);
        int replyCount = commentRepository.findByParentCommentId(comment.getId()).size();
        return commentMapper.toResponse(comment, authorName, replyCount);
    }
}
