package ai.zevaro.core.domain.requirement;

import ai.zevaro.core.domain.requirement.dto.CreateDependencyRequest;
import ai.zevaro.core.domain.requirement.dto.CreateRequirementRequest;
import ai.zevaro.core.domain.requirement.dto.DependencyResponse;
import ai.zevaro.core.domain.requirement.dto.RequirementResponse;
import ai.zevaro.core.domain.requirement.dto.UpdateRequirementRequest;
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
@RequestMapping("/api/v1")
@Tag(name = "Requirements", description = "Requirement management")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    // --- Nested endpoints (under Specifications) ---

    @PostMapping("/specifications/{specId}/requirements")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:create')")
    public ResponseEntity<RequirementResponse> create(
            @PathVariable UUID specId,
            @Valid @RequestBody CreateRequirementRequest request,
            @CurrentUser UserPrincipal user) {
        RequirementResponse response = requirementService.create(
                specId, request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/specifications/{specId}/requirements")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:read')")
    public ResponseEntity<List<RequirementResponse>> listBySpecification(
            @PathVariable UUID specId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(requirementService.listBySpecification(specId, user.getTenantId()));
    }

    @GetMapping("/specifications/{specId}/requirements/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:read')")
    public ResponseEntity<Page<RequirementResponse>> listBySpecificationPaged(
            @PathVariable UUID specId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(requirementService.listBySpecificationPaged(
                specId, user.getTenantId(), pageable));
    }

    // --- Flat endpoints (direct access) ---

    @GetMapping("/requirements/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:read')")
    public ResponseEntity<RequirementResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(requirementService.getById(id, user.getTenantId()));
    }

    @PutMapping("/requirements/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:update')")
    public ResponseEntity<RequirementResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequirementRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(requirementService.update(
                id, request, user.getTenantId(), user.getUserId()));
    }

    @DeleteMapping("/requirements/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        requirementService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    // --- Dependency sub-resource ---

    @PostMapping("/requirements/{id}/dependencies")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:update')")
    public ResponseEntity<DependencyResponse> addDependency(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDependencyRequest request,
            @CurrentUser UserPrincipal user) {
        DependencyResponse response = requirementService.addDependency(
                id, request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/requirements/{id}/dependencies")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:read')")
    public ResponseEntity<List<DependencyResponse>> getDependencies(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(requirementService.getDependencies(id, user.getTenantId()));
    }

    @GetMapping("/requirements/{id}/depended-on-by")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:read')")
    public ResponseEntity<List<DependencyResponse>> getDependedOnBy(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(requirementService.getDependedOnBy(id, user.getTenantId()));
    }

    @DeleteMapping("/requirements/{id}/dependencies/{depId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('requirement:update')")
    public ResponseEntity<Void> removeDependency(
            @PathVariable UUID id,
            @PathVariable UUID depId,
            @CurrentUser UserPrincipal user) {
        requirementService.removeDependency(id, depId, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
