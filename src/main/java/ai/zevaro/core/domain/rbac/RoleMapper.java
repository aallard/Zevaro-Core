package ai.zevaro.core.domain.rbac;

import ai.zevaro.core.domain.rbac.dto.RoleResponse;
import ai.zevaro.core.domain.rbac.dto.RoleSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role) {
        if (role == null) {
            return null;
        }
        List<String> permissionCodes = role.getPermissions().stream()
                .map(Permission::getCode)
                .sorted()
                .toList();

        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getCategory(),
                role.getLevel(),
                permissionCodes
        );
    }

    public RoleSummary toSummary(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleSummary(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getCategory(),
                role.getLevel()
        );
    }
}
