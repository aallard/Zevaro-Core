package ai.zevaro.core.domain.decision;

import ai.zevaro.core.domain.comment.dto.CommentResponse;
import ai.zevaro.core.domain.comment.dto.UpdateCommentRequest;
import ai.zevaro.core.domain.decision.dto.AddDecisionCommentRequest;
import ai.zevaro.core.domain.decision.dto.CastVoteRequest;
import ai.zevaro.core.domain.decision.dto.CreateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.DecisionQueueResponse;
import ai.zevaro.core.domain.decision.dto.DecisionResponse;
import ai.zevaro.core.domain.decision.dto.EscalateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.ResolveDecisionRequest;
import ai.zevaro.core.domain.decision.dto.UpdateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.VoteResponse;
import ai.zevaro.core.domain.decision.dto.VoteSummary;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1")
@Tag(name = "Decisions", description = "Decision management")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    // --- Nested endpoints (polymorphic parent) ---

    @GetMapping("/workstreams/{wsId}/decisions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForWorkstream(
            @PathVariable UUID wsId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.listByWorkstream(wsId, user.getTenantId()));
    }

    @GetMapping("/specifications/{specId}/decisions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForSpecification(
            @PathVariable UUID specId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.listByParent(
                DecisionParentType.SPECIFICATION, specId, user.getTenantId()));
    }

    @GetMapping("/requirements/{reqId}/decisions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForRequirement(
            @PathVariable UUID reqId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.listByParent(
                DecisionParentType.REQUIREMENT, reqId, user.getTenantId()));
    }

    @GetMapping("/tickets/{ticketId}/decisions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForTicket(
            @PathVariable UUID ticketId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.listByParent(
                DecisionParentType.TICKET, ticketId, user.getTenantId()));
    }

    // --- Flat endpoints ---

    @GetMapping("/decisions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisions(
            @RequestParam(required = false) DecisionStatus status,
            @RequestParam(required = false) DecisionPriority priority,
            @RequestParam(required = false) DecisionType type,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID projectId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisions(
                user.getTenantId(), status, priority, type, teamId, projectId));
    }

    @GetMapping("/decisions/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<Page<DecisionResponse>> getDecisionsPaged(
            @RequestParam(required = false) DecisionStatus status,
            @RequestParam(required = false) DecisionPriority priority,
            @RequestParam(required = false) DecisionType type,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID workstreamId,
            @RequestParam(required = false) DecisionParentType parentType,
            @RequestParam(required = false) String executionMode,
            @RequestParam(required = false) String slaStatus,
            @RequestParam(required = false) UUID portfolioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        if (programId != null || workstreamId != null || parentType != null
                || executionMode != null || slaStatus != null || portfolioId != null) {
            return ResponseEntity.ok(decisionService.listFiltered(
                    user.getTenantId(), programId, workstreamId, parentType,
                    executionMode, slaStatus, portfolioId, pageable));
        }

        return ResponseEntity.ok(decisionService.getDecisionsPaged(
                user.getTenantId(), status, priority, type, teamId, projectId, pageable));
    }

    @GetMapping("/decisions/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<DecisionResponse> getDecision(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean includeVotes,
            @RequestParam(defaultValue = "false") boolean includeComments,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionById(id, user.getTenantId(), includeVotes, includeComments));
    }

    @GetMapping("/decisions/queue")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<DecisionQueueResponse> getDecisionQueue(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionQueue(user.getTenantId()));
    }

    @GetMapping("/decisions/my-pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DecisionResponse>> getMyPendingDecisions(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getMyPendingDecisions(user.getUserId()));
    }

    @GetMapping("/decisions/pending")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getPendingDecisions(
            @RequestParam(required = false) UUID teamId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getPendingDecisions(user.getTenantId(), teamId));
    }

    @GetMapping("/decisions/blocking")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getBlockingDecisions(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getBlockingDecisions(user.getTenantId()));
    }

    @GetMapping("/decisions/overdue")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getOverdueDecisions(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getOverdueDecisions(user.getTenantId()));
    }

    @GetMapping("/decisions/outcome/{outcomeId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForOutcome(
            @PathVariable UUID outcomeId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionsForOutcome(outcomeId, user.getTenantId()));
    }

    @GetMapping("/decisions/hypothesis/{hypothesisId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<DecisionResponse>> getDecisionsForHypothesis(
            @PathVariable UUID hypothesisId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getDecisionsForHypothesis(hypothesisId, user.getTenantId()));
    }

    @PostMapping("/decisions")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:create')")
    public ResponseEntity<DecisionResponse> createDecision(
            @Valid @RequestBody CreateDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        DecisionResponse decision = decisionService.createDecision(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(decision);
    }

    @PutMapping("/decisions/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> updateDecision(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.updateDecision(id, user.getTenantId(), request));
    }

    @DeleteMapping("/decisions/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:delete')")
    public ResponseEntity<Void> deleteDecision(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        decisionService.deleteDecision(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/decisions/{id}/start-discussion")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> startDiscussion(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.startDiscussion(id, user.getTenantId()));
    }

    @PostMapping("/decisions/{id}/resolve")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:resolve')")
    public ResponseEntity<DecisionResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.resolve(id, user.getTenantId(), request, user.getUserId()));
    }

    @PostMapping("/decisions/{id}/implement")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> implement(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.implement(id, user.getTenantId()));
    }

    @PostMapping("/decisions/{id}/defer")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> defer(
            @PathVariable UUID id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.defer(id, user.getTenantId(), reason));
    }

    @PostMapping("/decisions/{id}/cancel")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:delete')")
    public ResponseEntity<DecisionResponse> cancel(
            @PathVariable UUID id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.cancel(id, user.getTenantId(), reason));
    }

    @PostMapping("/decisions/{id}/reopen")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:update')")
    public ResponseEntity<DecisionResponse> reopen(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.reopen(id, user.getTenantId()));
    }

    @PostMapping("/decisions/{id}/escalate")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:escalate')")
    public ResponseEntity<DecisionResponse> escalate(
            @PathVariable UUID id,
            @Valid @RequestBody EscalateDecisionRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.escalate(id, user.getTenantId(), request, user.getUserId()));
    }

    @PostMapping("/decisions/{id}/assign")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:assign')")
    public ResponseEntity<DecisionResponse> assign(
            @PathVariable UUID id,
            @RequestParam UUID assigneeId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.assign(id, user.getTenantId(), assigneeId));
    }

    @PostMapping("/decisions/{id}/reassign")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:assign')")
    public ResponseEntity<DecisionResponse> reassign(
            @PathVariable UUID id,
            @RequestParam UUID newAssigneeId,
            @RequestParam(required = false) String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.reassign(id, user.getTenantId(), newAssigneeId, reason));
    }

    @GetMapping("/decisions/{id}/comments")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getComments(id, user.getTenantId()));
    }

    @PostMapping("/decisions/{id}/comments")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:comment')")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody AddDecisionCommentRequest request,
            @CurrentUser UserPrincipal user) {
        CommentResponse comment = decisionService.addComment(
                id, user.getTenantId(), request.body(), request.parentCommentId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/decisions/{id}/comments/{commentId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:comment')")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.updateComment(commentId, user.getTenantId(), request, user.getUserId()));
    }

    @DeleteMapping("/decisions/{id}/comments/{commentId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:comment')")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @CurrentUser UserPrincipal user) {
        decisionService.deleteComment(commentId, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/decisions/stats")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<Map<DecisionStatus, Long>> getStatusCounts(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getStatusCounts(user.getTenantId()));
    }

    @GetMapping("/decisions/metrics/avg-time")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('analytics:read')")
    public ResponseEntity<Double> getAverageDecisionTime(
            @RequestParam(defaultValue = "30") int days,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getAverageDecisionTime(user.getTenantId(), days));
    }

    // Vote endpoints

    @GetMapping("/decisions/{id}/votes")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<List<VoteResponse>> getVotes(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getVotes(id, user.getTenantId()));
    }

    @GetMapping("/decisions/{id}/votes/summary")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:read')")
    public ResponseEntity<VoteSummary> getVoteSummary(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(decisionService.getVoteSummary(id, user.getTenantId()));
    }

    @PostMapping("/decisions/{id}/votes")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:vote')")
    public ResponseEntity<VoteResponse> castVote(
            @PathVariable UUID id,
            @Valid @RequestBody CastVoteRequest request,
            @CurrentUser UserPrincipal user) {
        VoteResponse vote = decisionService.castVote(id, user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(vote);
    }

    @DeleteMapping("/decisions/{id}/votes")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('decision:vote')")
    public ResponseEntity<Void> removeVote(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        decisionService.removeVote(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
