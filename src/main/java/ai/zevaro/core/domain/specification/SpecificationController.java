package ai.zevaro.core.domain.specification;

import ai.zevaro.core.domain.specification.dto.CreateSpecificationRequest;
import ai.zevaro.core.domain.specification.dto.SpecificationResponse;
import ai.zevaro.core.domain.specification.dto.UpdateSpecificationRequest;
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
@Tag(name = "Specifications", description = "Specification management")
@RequiredArgsConstructor
public class SpecificationController {

    private final SpecificationService specificationService;

    // --- Nested endpoints (under Workstreams) ---

    @PostMapping("/workstreams/{wsId}/specifications")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:create')")
    public ResponseEntity<SpecificationResponse> create(
            @PathVariable UUID wsId,
            @Valid @RequestBody CreateSpecificationRequest request,
            @CurrentUser UserPrincipal user) {
        SpecificationResponse response = specificationService.create(
                wsId, request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/workstreams/{wsId}/specifications")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:read')")
    public ResponseEntity<List<SpecificationResponse>> listByWorkstream(
            @PathVariable UUID wsId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.listByWorkstream(wsId, user.getTenantId()));
    }

    @GetMapping("/workstreams/{wsId}/specifications/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:read')")
    public ResponseEntity<Page<SpecificationResponse>> listByWorkstreamPaged(
            @PathVariable UUID wsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(specificationService.listByWorkstreamPaged(
                wsId, user.getTenantId(), pageable));
    }

    // --- Flat endpoints (direct access) ---

    @GetMapping("/specifications/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:read')")
    public ResponseEntity<SpecificationResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.getById(id, user.getTenantId()));
    }

    @PutMapping("/specifications/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:update')")
    public ResponseEntity<SpecificationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSpecificationRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.update(
                id, request, user.getTenantId(), user.getUserId()));
    }

    @DeleteMapping("/specifications/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        specificationService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }

    // --- Workflow endpoints ---

    @PostMapping("/specifications/{id}/submit-review")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:update')")
    public ResponseEntity<SpecificationResponse> submitForReview(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.submitForReview(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/specifications/{id}/approve")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:approve')")
    public ResponseEntity<SpecificationResponse> approve(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.approve(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/specifications/{id}/reject")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:update')")
    public ResponseEntity<SpecificationResponse> reject(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.reject(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/specifications/{id}/start-work")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:update')")
    public ResponseEntity<SpecificationResponse> startWork(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.startWork(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/specifications/{id}/deliver")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:update')")
    public ResponseEntity<SpecificationResponse> deliver(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.markDelivered(
                id, user.getTenantId(), user.getUserId()));
    }

    @PostMapping("/specifications/{id}/accept")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('specification:approve')")
    public ResponseEntity<SpecificationResponse> accept(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(specificationService.markAccepted(
                id, user.getTenantId(), user.getUserId()));
    }
}
