package ai.zevaro.core.domain.project;

import ai.zevaro.core.domain.project.dto.CreateProjectRequest;
import ai.zevaro.core.domain.project.dto.ProjectDashboardResponse;
import ai.zevaro.core.domain.project.dto.ProjectResponse;
import ai.zevaro.core.domain.project.dto.ProjectStatsResponse;
import ai.zevaro.core.domain.project.dto.UpdateProjectRequest;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @RequestParam(required = false) ProjectStatus status,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(projectService.getProjects(user.getTenantId(), status));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<Page<ProjectResponse>> getProjectsPaged(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(projectService.getProjectsPaged(user.getTenantId(), status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(projectService.getProjectById(id, user.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:create')")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @CurrentUser UserPrincipal user) {
        ProjectResponse project = projectService.createProject(
                user.getTenantId(), request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:update')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(projectService.updateProject(id, user.getTenantId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:delete')")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        projectService.deleteProject(id, user.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<ProjectStatsResponse> getProjectStats(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(projectService.getProjectStats(id, user.getTenantId()));
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('project:read')")
    public ResponseEntity<ProjectDashboardResponse> getProjectDashboard(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(projectService.getProjectDashboard(id, user.getTenantId()));
    }
}
