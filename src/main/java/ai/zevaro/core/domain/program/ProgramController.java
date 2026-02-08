package ai.zevaro.core.domain.program;

import ai.zevaro.core.domain.program.dto.CreateProgramRequest;
import ai.zevaro.core.domain.program.dto.ProgramDashboardResponse;
import ai.zevaro.core.domain.program.dto.ProgramResponse;
import ai.zevaro.core.domain.program.dto.ProgramStatsResponse;
import ai.zevaro.core.domain.program.dto.UpdateProgramRequest;
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
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<List<ProgramResponse>> getPrograms(
            @RequestParam(required = false) ProgramStatus status,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(programService.getPrograms(user.getTenantId(), status));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<Page<ProgramResponse>> getProgramsPaged(
            @RequestParam(required = false) ProgramStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(programService.getProgramsPaged(user.getTenantId(), status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<ProgramResponse> getProgram(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(programService.getProgramById(id, user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:create')")
    public ResponseEntity<ProgramResponse> createProgram(
            @Valid @RequestBody CreateProgramRequest request,
            @CurrentUser UserPrincipal user) {
        ProgramResponse program = programService.createProgram(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(program);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:update')")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProgramRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(programService.updateProgram(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:delete')")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        programService.deleteProgram(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<ProgramStatsResponse> getProgramStats(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(programService.getProgramStats(id, user.getTenantId()));
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<ProgramDashboardResponse> getProgramDashboard(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(programService.getProgramDashboard(id, user.getTenantId()));
    }
}
