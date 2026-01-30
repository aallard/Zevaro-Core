package ai.zevaro.core.domain.rbac;

import ai.zevaro.core.domain.rbac.dto.CreateRoleRequest;
import ai.zevaro.core.domain.rbac.dto.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAvailableRoles(UUID tenantId) {
        return roleRepository.findByTenantIdIsNullOrTenantId(tenantId).stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByCategory(RoleCategory category) {
        return roleRepository.findByCategoryAndTenantIdIsNull(category).stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));
        return roleMapper.toResponse(role);
    }

    @Transactional
    public RoleResponse createCustomRole(UUID tenantId, CreateRoleRequest request) {
        if (roleRepository.findByCodeAndTenantId(request.code(), tenantId).isPresent()) {
            throw new IllegalArgumentException("Role with code already exists: " + request.code());
        }

        Role role = new Role();
        role.setTenantId(tenantId);
        role.setCode(request.code());
        role.setName(request.name());
        role.setDescription(request.description());
        role.setCategory(RoleCategory.CUSTOM);
        role.setLevel(request.level());
        role.setSystemRole(false);

        if (request.permissionCodes() != null && !request.permissionCodes().isEmpty()) {
            Set<Permission> permissions = new HashSet<>(
                    permissionRepository.findByCodeIn(request.permissionCodes())
            );
            role.setPermissions(permissions);
        }

        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Transactional(readOnly = true)
    public List<RoleCategory> getCategories() {
        return List.of(RoleCategory.values());
    }
}
