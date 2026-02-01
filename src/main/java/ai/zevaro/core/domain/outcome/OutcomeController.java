package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.outcome.dto.CreateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.InvalidateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.OutcomeResponse;
import ai.zevaro.core.domain.outcome.dto.UpdateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.ValidateOutcomeRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/v1/outcomes")
@RequiredArgsConstructor
public class OutcomeController {

    private final OutcomeService outcomeService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<List<OutcomeResponse>> getOutcomes(
            @RequestParam(required = false) OutcomeStatus status,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) OutcomePriority priority,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.getOutcomes(
                user.getTenantId(), status, teamId, priority));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<Page<OutcomeResponse>> getOutcomesPaged(
            @RequestParam(required = false) OutcomeStatus status,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) OutcomePriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(outcomeService.getOutcomesPaged(
                user.getTenantId(), status, teamId, priority, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<OutcomeResponse> getOutcome(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.getOutcomeById(id, user.getTenantId()));
    }

    @GetMapping("/my-outcomes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OutcomeResponse>> getMyOutcomes(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.getOutcomesForOwner(user.getUserId(), user.getTenantId()));
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<List<OutcomeResponse>> getTeamOutcomes(
            @PathVariable UUID teamId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.getOutcomesForTeam(teamId, user.getTenantId()));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<List<OutcomeResponse>> getOverdueOutcomes(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.getOverdueOutcomes(user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:create')")
    public ResponseEntity<OutcomeResponse> createOutcome(
            @Valid @RequestBody CreateOutcomeRequest request,
            @CurrentUser UserPrincipal user) {
        OutcomeResponse outcome = outcomeService.createOutcome(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(outcome);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:update')")
    public ResponseEntity<OutcomeResponse> updateOutcome(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOutcomeRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.updateOutcome(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:delete')")
    public ResponseEntity<Void> deleteOutcome(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        outcomeService.deleteOutcome(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:update')")
    public ResponseEntity<OutcomeResponse> startOutcome(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.startOutcome(id, user.getTenantId()));
    }

    @PostMapping("/{id}/validate")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:validate')")
    public ResponseEntity<OutcomeResponse> validateOutcome(
            @PathVariable UUID id,
            @Valid @RequestBody ValidateOutcomeRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.validateOutcome(
                id, user.getTenantId(), request, user.getUserId()));
    }

    @PostMapping("/{id}/invalidate")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:validate')")
    public ResponseEntity<OutcomeResponse> invalidateOutcome(
            @PathVariable UUID id,
            @Valid @RequestBody InvalidateOutcomeRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.invalidateOutcome(
                id, user.getTenantId(), request, user.getUserId()));
    }

    @PostMapping("/{id}/abandon")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:delete')")
    public ResponseEntity<OutcomeResponse> abandonOutcome(
            @PathVariable UUID id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.abandonOutcome(id, user.getTenantId(), reason));
    }

    @PatchMapping("/{id}/metrics")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:update')")
    public ResponseEntity<OutcomeResponse> updateMetrics(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> currentMetrics,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.updateMetrics(id, user.getTenantId(), currentMetrics));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<Map<OutcomeStatus, Long>> getStatusCounts(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(outcomeService.getStatusCounts(user.getTenantId()));
    }
}
