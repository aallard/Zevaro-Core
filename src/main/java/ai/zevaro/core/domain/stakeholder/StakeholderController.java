package ai.zevaro.core.domain.stakeholder;

import ai.zevaro.core.domain.decision.dto.DecisionResponse;
import ai.zevaro.core.domain.stakeholder.dto.CreateStakeholderRequest;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderLeaderboard;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderMetrics;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderResponse;
import ai.zevaro.core.domain.stakeholder.dto.UpdateStakeholderRequest;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stakeholders")
@RequiredArgsConstructor
public class StakeholderController {

    private final StakeholderService stakeholderService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:read')")
    public ResponseEntity<List<StakeholderResponse>> getStakeholders(
            @RequestParam(required = false) StakeholderType type,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getStakeholders(user.getTenantId(), type, activeOnly));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:read')")
    public ResponseEntity<StakeholderResponse> getStakeholder(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getStakeholderById(id, user.getTenantId()));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:read')")
    public ResponseEntity<StakeholderResponse> getStakeholderByEmail(
            @PathVariable String email,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getStakeholderByEmail(email, user.getTenantId()));
    }

    @GetMapping("/with-pending")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:read')")
    public ResponseEntity<List<StakeholderResponse>> getStakeholdersWithPending(
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getStakeholdersWithPendingDecisions(user.getTenantId()));
    }

    @GetMapping("/slow-responders")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('analytics:read')")
    public ResponseEntity<List<StakeholderResponse>> getSlowResponders(
            @RequestParam(defaultValue = "24") double thresholdHours,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getSlowResponders(user.getTenantId(), thresholdHours));
    }

    @GetMapping("/search/expertise")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:read')")
    public ResponseEntity<List<StakeholderResponse>> findByExpertise(
            @RequestParam String expertise,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.findByExpertise(user.getTenantId(), expertise));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:create')")
    public ResponseEntity<StakeholderResponse> createStakeholder(
            @Valid @RequestBody CreateStakeholderRequest request,
            @CurrentUser UserPrincipal user) {
        StakeholderResponse stakeholder = stakeholderService.createStakeholder(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(stakeholder);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:update')")
    public ResponseEntity<StakeholderResponse> updateStakeholder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStakeholderRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.updateStakeholder(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('stakeholder:delete')")
    public ResponseEntity<Void> deleteStakeholder(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        stakeholderService.deleteStakeholder(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/metrics")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('analytics:read')")
    public ResponseEntity<StakeholderMetrics> getStakeholderMetrics(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getStakeholderMetrics(id, user.getTenantId()));
    }

    @GetMapping("/leaderboard")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('analytics:read')")
    public ResponseEntity<StakeholderLeaderboard> getLeaderboard(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getLeaderboard(user.getTenantId()));
    }

    @GetMapping("/me/responses/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DecisionResponse>> getMyPendingResponses(@CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(stakeholderService.getMyPendingResponses(user.getUserId(), user.getTenantId()));
    }
}
