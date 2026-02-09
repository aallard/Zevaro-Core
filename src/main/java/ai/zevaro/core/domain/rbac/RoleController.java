package ai.zevaro.core.domain.rbac;

import ai.zevaro.core.domain.rbac.dto.CreateRoleRequest;
import ai.zevaro.core.domain.rbac.dto.RoleResponse;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Role-based access control")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:read')")
    public ResponseEntity<List<RoleResponse>> getAvailableRoles(@CurrentUser UserPrincipal currentUser) {
        List<RoleResponse> roles = roleService.getAvailableRoles(currentUser.getTenantId());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:read')")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable UUID id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleResponse> createCustomRole(
            @Valid @RequestBody CreateRoleRequest request,
            @CurrentUser UserPrincipal currentUser) {
        RoleResponse role = roleService.createCustomRole(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:read')")
    public ResponseEntity<List<RoleCategory>> getCategories() {
        return ResponseEntity.ok(roleService.getCategories());
    }
}
