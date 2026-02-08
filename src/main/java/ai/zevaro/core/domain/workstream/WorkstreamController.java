package ai.zevaro.core.domain.workstream;

import ai.zevaro.core.domain.workstream.dto.CreateWorkstreamRequest;
import ai.zevaro.core.domain.workstream.dto.UpdateWorkstreamRequest;
import ai.zevaro.core.domain.workstream.dto.WorkstreamResponse;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WorkstreamController {

    private final WorkstreamService workstreamService;

    // --- Nested endpoints (under Programs) ---

    @PostMapping("/programs/{programId}/workstreams")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('workstream:create')")
    public ResponseEntity<WorkstreamResponse> create(
            @PathVariable UUID programId,
            @Valid @RequestBody CreateWorkstreamRequest request,
            @CurrentUser UserPrincipal user) {
        WorkstreamResponse response = workstreamService.create(
                programId, request, user.getTenantId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/programs/{programId}/workstreams")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('workstream:read')")
    public ResponseEntity<List<WorkstreamResponse>> listByProgram(
            @PathVariable UUID programId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(workstreamService.listByProgram(programId, user.getTenantId()));
    }

    @GetMapping("/programs/{programId}/workstreams/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('workstream:read')")
    public ResponseEntity<Page<WorkstreamResponse>> listByProgramPaged(
            @PathVariable UUID programId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(workstreamService.listByProgramPaged(
                programId, user.getTenantId(), pageable));
    }

    // --- Flat endpoints (direct access by workstream ID) ---

    @GetMapping("/workstreams/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('workstream:read')")
    public ResponseEntity<WorkstreamResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(workstreamService.getById(id, user.getTenantId()));
    }

    @PutMapping("/workstreams/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('workstream:update')")
    public ResponseEntity<WorkstreamResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkstreamRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(workstreamService.update(
                id, request, user.getTenantId(), user.getUserId()));
    }

    @DeleteMapping("/workstreams/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('workstream:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        workstreamService.delete(id, user.getTenantId(), user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
