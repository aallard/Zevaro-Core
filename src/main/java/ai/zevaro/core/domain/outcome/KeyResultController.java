package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.outcome.dto.CreateKeyResultRequest;
import ai.zevaro.core.domain.outcome.dto.KeyResultResponse;
import ai.zevaro.core.domain.outcome.dto.UpdateKeyResultRequest;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/outcomes/{outcomeId}/key-results")
@RequiredArgsConstructor
public class KeyResultController {

    private final KeyResultService keyResultService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<List<KeyResultResponse>> getKeyResults(
            @PathVariable UUID outcomeId,
            @CurrentUser UserPrincipal principal) {
        List<KeyResultResponse> keyResults = keyResultService.getKeyResultsForOutcome(outcomeId, principal.getTenantId());
        return ResponseEntity.ok(keyResults);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:read')")
    public ResponseEntity<KeyResultResponse> getKeyResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal) {
        KeyResultResponse keyResult = keyResultService.getKeyResultById(id, outcomeId, principal.getTenantId());
        return ResponseEntity.ok(keyResult);
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:update')")
    public ResponseEntity<KeyResultResponse> createKeyResult(
            @PathVariable UUID outcomeId,
            @Valid @RequestBody CreateKeyResultRequest request,
            @CurrentUser UserPrincipal principal) {
        KeyResultResponse keyResult = keyResultService.createKeyResult(outcomeId, principal.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(keyResult);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:update')")
    public ResponseEntity<KeyResultResponse> updateKeyResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateKeyResultRequest request,
            @CurrentUser UserPrincipal principal) {
        KeyResultResponse keyResult = keyResultService.updateKeyResult(id, outcomeId, principal.getTenantId(), request);
        return ResponseEntity.ok(keyResult);
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:update')")
    public ResponseEntity<KeyResultResponse> updateProgress(
            @PathVariable UUID outcomeId,
            @PathVariable UUID id,
            @RequestParam BigDecimal value,
            @CurrentUser UserPrincipal principal) {
        KeyResultResponse keyResult = keyResultService.updateProgress(id, outcomeId, principal.getTenantId(), value);
        return ResponseEntity.ok(keyResult);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('outcome:update')")
    public ResponseEntity<Void> deleteKeyResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal) {
        keyResultService.deleteKeyResult(id, outcomeId, principal.getTenantId());
        return ResponseEntity.noContent().build();
    }
}
