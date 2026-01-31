package ai.zevaro.core.domain.decision;

import ai.zevaro.core.domain.decision.dto.CommentResponse;
import ai.zevaro.core.domain.decision.dto.CreateCommentRequest;
import ai.zevaro.core.domain.decision.dto.CreateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.DecisionQueueResponse;
import ai.zevaro.core.domain.decision.dto.DecisionResponse;
import ai.zevaro.core.domain.decision.dto.EscalateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.ResolveDecisionRequest;
import ai.zevaro.core.domain.decision.dto.UpdateCommentRequest;
import ai.zevaro.core.domain.decision.dto.UpdateDecisionRequest;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/decisions")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisions(
            @RequestParam(required = false) DecisionStatus status,
            @RequestParam(required = false) DecisionPriority priority,
            @RequestParam(required = false) DecisionType type,
            @RequestParam(required = false) UUID teamId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisions(
                user.getTenantId(), status, priority, type, teamId));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<Page<DecisionResponse>> getDecisionsPaged(
            @RequestParam(required = false) DecisionStatus status,
            @RequestParam(required = false) DecisionPriority priority,
            @RequestParam(required = false) DecisionType type,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(decisionService.getDecisionsPaged(
                user.getTenantId(), status, priority, type, teamId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<DecisionResponse> getDecision(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionById(id, user.getTenantId()));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<DecisionQueueResponse> getDecisionQueue(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionQueue(user.getTenantId()));
    }

    @GetMapping("/my-pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DecisionResponse>> getMyPendingDecisions(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getMyPendingDecisions(user.getUserId()));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getOverdueDecisions(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getOverdueDecisions(user.getTenantId()));
    }

    @GetMapping("/outcome/{outcomeId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForOutcome(
            @PathVariable UUID outcomeId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionsForOutcome(outcomeId, user.getTenantId()));
    }

    @GetMapping("/hypothesis/{hypothesisId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForHypothesis(
            @PathVariable UUID hypothesisId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionsForHypothesis(hypothesisId, user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:create')")
    public ResponseEntity<DecisionResponse> createDecision(
            @Valid @RequestBody CreateDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        DecisionResponse decision = decisionService.createDecision(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(decision);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> updateDecision(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.updateDecision(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:delete')")
    public ResponseEntity<Void> deleteDecision(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        decisionService.deleteDecision(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start-discussion")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> startDiscussion(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.startDiscussion(id, user.getTenantId()));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:resolve')")
    public ResponseEntity<DecisionResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.resolve(id, user.getTenantId(), request, user.getUserId()));
    }

    @PostMapping("/{id}/implement")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> implement(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.implement(id, user.getTenantId()));
    }

    @PostMapping("/{id}/defer")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> defer(
            @PathVariable UUID id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.defer(id, user.getTenantId(), reason));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:delete')")
    public ResponseEntity<DecisionResponse> cancel(
            @PathVariable UUID id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.cancel(id, user.getTenantId(), reason));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> reopen(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.reopen(id, user.getTenantId()));
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:escalate')")
    public ResponseEntity<DecisionResponse> escalate(
            @PathVariable UUID id,
            @Valid @RequestBody EscalateDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.escalate(id, user.getTenantId(), request, user.getUserId()));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:assign')")
    public ResponseEntity<DecisionResponse> assign(
            @PathVariable UUID id,
            @RequestParam UUID assigneeId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.assign(id, user.getTenantId(), assigneeId));
    }

    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:assign')")
    public ResponseEntity<DecisionResponse> reassign(
            @PathVariable UUID id,
            @RequestParam UUID newAssigneeId,
            @RequestParam(required = false) String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.reassign(id, user.getTenantId(), newAssigneeId, reason));
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getComments(id, user.getTenantId()));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:comment')")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCommentRequest request,
            @CurrentUser UserPrincipal user) {
        CommentResponse comment = decisionService.addComment(id, user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{id}/comments/{commentId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:comment')")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.updateComment(commentId, user.getTenantId(), request, user.getUserId()));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:comment')")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @CurrentUser UserPrincipal user) {
        decisionService.deleteComment(commentId, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<Map<DecisionStatus, Long>> getStatusCounts(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getStatusCounts(user.getTenantId()));
    }

    @GetMapping("/metrics/avg-time")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('analytics:read')")
    public ResponseEntity<Double> getAverageDecisionTime(
            @RequestParam(defaultValue = "30") int days,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getAverageDecisionTime(user.getTenantId(), days));
    }
}
