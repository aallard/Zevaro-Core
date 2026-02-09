package ai.zevaro.core.domain.user;

import ai.zevaro.core.domain.user.dto.UpdateUserRequest;
import ai.zevaro.core.domain.user.dto.UserResponse;
import ai.zevaro.core.security.CurrentUser;
import ai.zevaro.core.security.UserPrincipal;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:read')")
    public ResponseEntity<List<UserResponse>> getUsers(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String department) {
        List<UserResponse> users;
        if (department != null) {
            users = userService.getUsersByDepartment(currentUser.getTenantId(), department);
        } else {
            users = userService.getUsers(currentUser.getTenantId());
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:read')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        UserResponse user = userService.getUserById(id, currentUser.getTenantId());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:update')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @CurrentUser UserPrincipal currentUser) {
        UserResponse user = userService.updateUser(id, currentUser.getTenantId(), request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:delete')")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        userService.deactivateUser(id, currentUser.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserResponse user = userService.getUserById(currentUser.getUserId(), currentUser.getTenantId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/direct-reports")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:read')")
    public ResponseEntity<List<UserResponse>> getDirectReports(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        List<UserResponse> reports = userService.getDirectReports(id, currentUser.getTenantId());
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('TENANT_ADMIN') or hasRole('SUPER_ADMIN') or hasAuthority('user:manage_roles')")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable UUID id,
            @RequestParam UUID roleId,
            @CurrentUser UserPrincipal currentUser) {
        UserResponse user = userService.assignRole(id, roleId, currentUser.getTenantId());
        return ResponseEntity.ok(user);
    }
}
