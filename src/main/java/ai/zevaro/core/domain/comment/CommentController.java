package ai.zevaro.core.domain.comment;

import ai.zevaro.core.domain.comment.dto.CommentResponse;
import ai.zevaro.core.domain.comment.dto.CreateCommentRequest;
import ai.zevaro.core.domain.comment.dto.UpdateCommentRequest;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @CurrentUser UserPrincipal user) {
        CommentResponse comment = commentService.create(request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/by-parent/{parentType}/{parentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentResponse>> getCommentsByParent(
            @PathVariable CommentParentType parentType,
            @PathVariable UUID parentId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(commentService.getByParent(parentType, parentId, user.getTenantId()));
    }

    @GetMapping("/by-parent/{parentType}/{parentId}/paged")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CommentResponse>> getCommentsByParentPaged(
            @PathVariable CommentParentType parentType,
            @PathVariable UUID parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(commentService.getByParentPaged(parentType, parentId, user.getTenantId(), pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(commentService.update(id, request, user.getTenantId(), user.getUserId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        commentService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentResponse>> getReplies(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(commentService.getReplies(id, user.getTenantId()));
    }
}
