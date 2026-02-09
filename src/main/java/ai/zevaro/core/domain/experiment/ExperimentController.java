package ai.zevaro.core.domain.experiment;

import ai.zevaro.core.domain.experiment.dto.CreateExperimentRequest;
import ai.zevaro.core.domain.experiment.dto.ExperimentResponse;
import ai.zevaro.core.domain.experiment.dto.RecordResultsRequest;
import ai.zevaro.core.domain.experiment.dto.UpdateExperimentRequest;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/experiments")
@Tag(name = "Experiments", description = "Experiment management")
@RequiredArgsConstructor
public class ExperimentController {

    private final ExperimentService experimentService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:read')")
    public ResponseEntity<List<ExperimentResponse>> listExperiments(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) ExperimentStatus status,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.listExperiments(
                user.getTenantId(), projectId, status));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:read')")
    public ResponseEntity<Page<ExperimentResponse>> listExperimentsPaged(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) ExperimentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(experimentService.listExperimentsPaged(
                user.getTenantId(), projectId, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:read')")
    public ResponseEntity<ExperimentResponse> getExperiment(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.getExperiment(id, user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:create')")
    public ResponseEntity<ExperimentResponse> createExperiment(
            @Valid @RequestBody CreateExperimentRequest request,
            @CurrentUser UserPrincipal user) {
        ExperimentResponse experiment = experimentService.createExperiment(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(experiment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:update')")
    public ResponseEntity<ExperimentResponse> updateExperiment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExperimentRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.updateExperiment(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:delete')")
    public ResponseEntity<Void> deleteExperiment(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        experimentService.deleteExperiment(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:update')")
    public ResponseEntity<ExperimentResponse> startExperiment(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.startExperiment(id, user.getTenantId()));
    }

    @PostMapping("/{id}/conclude")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:update')")
    public ResponseEntity<ExperimentResponse> concludeExperiment(
            @PathVariable UUID id,
            @RequestParam String conclusion,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.concludeExperiment(id, user.getTenantId(), conclusion));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:update')")
    public ResponseEntity<ExperimentResponse> cancelExperiment(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.cancelExperiment(id, user.getTenantId()));
    }

    @PostMapping("/{id}/extend")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:update')")
    public ResponseEntity<ExperimentResponse> extendExperiment(
            @PathVariable UUID id,
            @RequestParam Integer additionalDays,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.extendExperiment(id, user.getTenantId(), additionalDays));
    }

    @PostMapping("/{id}/results")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:update')")
    public ResponseEntity<ExperimentResponse> recordResults(
            @PathVariable UUID id,
            @Valid @RequestBody RecordResultsRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.recordResults(id, user.getTenantId(), request));
    }

    @GetMapping("/hypothesis/{hypothesisId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('experiment:read')")
    public ResponseEntity<List<ExperimentResponse>> getExperimentsByHypothesis(
            @PathVariable UUID hypothesisId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(experimentService.getExperimentsByHypothesis(hypothesisId, user.getTenantId()));
    }
}
