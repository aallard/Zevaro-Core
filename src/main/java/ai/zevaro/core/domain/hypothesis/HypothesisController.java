package ai.zevaro.core.domain.hypothesis;

import ai.zevaro.core.domain.hypothesis.dto.ConcludeHypothesisRequest;
import ai.zevaro.core.domain.hypothesis.dto.CreateHypothesisRequest;
import ai.zevaro.core.domain.hypothesis.dto.HypothesisResponse;
import ai.zevaro.core.domain.hypothesis.dto.TransitionHypothesisRequest;
import ai.zevaro.core.domain.hypothesis.dto.UpdateHypothesisRequest;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hypotheses")
@RequiredArgsConstructor
public class HypothesisController {

    private final HypothesisService hypothesisService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<List<HypothesisResponse>> getHypotheses(
            @RequestParam(required = false) HypothesisStatus status,
            @RequestParam(required = false) UUID outcomeId,
            @RequestParam(required = false) HypothesisPriority priority,
            @RequestParam(required = false) UUID projectId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getHypotheses(
                user.getTenantId(), status, outcomeId, priority, projectId));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<Page<HypothesisResponse>> getHypothesesPaged(
            @RequestParam(required = false) HypothesisStatus status,
            @RequestParam(required = false) HypothesisPriority priority,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser UserPrincipal user) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(hypothesisService.getHypothesesPaged(
                user.getTenantId(), status, priority, projectId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<HypothesisResponse> getHypothesis(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getHypothesisById(id, user.getTenantId()));
    }

    @GetMapping("/my-hypotheses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HypothesisResponse>> getMyHypotheses(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getHypothesesForOwner(user.getUserId(), user.getTenantId()));
    }

    @GetMapping("/outcome/{outcomeId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<List<HypothesisResponse>> getOutcomeHypotheses(
            @PathVariable UUID outcomeId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getHypothesesForOutcome(outcomeId, user.getTenantId()));
    }

    @GetMapping("/blocked")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<List<HypothesisResponse>> getBlockedHypotheses(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getBlockedHypotheses(user.getTenantId()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<List<HypothesisResponse>> getActiveHypotheses(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getActiveHypotheses(user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:create')")
    public ResponseEntity<HypothesisResponse> createHypothesis(
            @Valid @RequestBody CreateHypothesisRequest request,
            @CurrentUser UserPrincipal user) {
        HypothesisResponse hypothesis = hypothesisService.createHypothesis(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(hypothesis);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:update')")
    public ResponseEntity<HypothesisResponse> updateHypothesis(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHypothesisRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.updateHypothesis(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:delete')")
    public ResponseEntity<Void> deleteHypothesis(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        hypothesisService.deleteHypothesis(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/transition")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:update')")
    public ResponseEntity<HypothesisResponse> transitionHypothesis(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionHypothesisRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.transitionHypothesis(id, user.getTenantId(), request));
    }

    @PostMapping("/{id}/conclude")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:validate')")
    public ResponseEntity<HypothesisResponse> concludeHypothesis(
            @PathVariable UUID id,
            @Valid @RequestBody ConcludeHypothesisRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.concludeHypothesis(
                id, user.getTenantId(), request, user.getUserId()));
    }

    @PostMapping("/{id}/abandon")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:delete')")
    public ResponseEntity<HypothesisResponse> abandonHypothesis(
            @PathVariable UUID id,
            @RequestParam String reason,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.abandonHypothesis(id, user.getTenantId(), reason));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<Map<HypothesisStatus, Long>> getStatusCounts(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getStatusCounts(user.getTenantId()));
    }

    @GetMapping("/outcome/{outcomeId}/stats")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('hypothesis:read')")
    public ResponseEntity<Map<HypothesisStatus, Long>> getOutcomeStatusCounts(
            @PathVariable UUID outcomeId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(hypothesisService.getStatusCountsForOutcome(outcomeId));
    }
}
