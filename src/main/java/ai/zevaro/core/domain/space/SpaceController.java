package ai.zevaro.core.domain.space;

import ai.zevaro.core.domain.space.dto.CreateSpaceRequest;
import ai.zevaro.core.domain.space.dto.SpaceResponse;
import ai.zevaro.core.domain.space.dto.UpdateSpaceRequest;
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
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:create')")
    public ResponseEntity<SpaceResponse> create(
            @Valid @RequestBody CreateSpaceRequest request,
            @CurrentUser UserPrincipal user) {
        SpaceResponse response = spaceService.create(request, user.getTenantId(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:read')")
    public ResponseEntity<List<SpaceResponse>> list(
            @RequestParam(required = false) SpaceType type,
            @CurrentUser UserPrincipal user) {
        if (type != null) {
            return ResponseEntity.ok(spaceService.listByType(user.getTenantId(), type));
        }
        return ResponseEntity.ok(spaceService.list(user.getTenantId()));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:read')")
    public ResponseEntity<Page<SpaceResponse>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal user) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);
        return ResponseEntity.ok(spaceService.listPaged(user.getTenantId(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:read')")
    public ResponseEntity<SpaceResponse> getById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(spaceService.getById(id, user.getTenantId()));
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:read')")
    public ResponseEntity<SpaceResponse> getBySlug(
            @PathVariable String slug,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(spaceService.getBySlug(slug, user.getTenantId()));
    }

    @GetMapping("/program/{programId}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:read')")
    public ResponseEntity<SpaceResponse> getByProgramId(
            @PathVariable UUID programId,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(spaceService.getByProgramId(programId, user.getTenantId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:update')")
    public ResponseEntity<SpaceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSpaceRequest request,
            @CurrentUser UserPrincipal user) {
        return ResponseEntity.ok(spaceService.update(id, request, user.getTenantId(), user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_OWNER') or hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('space:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal user) {
        spaceService.delete(id, user.getTenantId(), user);
        return ResponseEntity.noContent().build();
    }
}
