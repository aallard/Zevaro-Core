package ai.zevaro.core.domain.rbac.dto;

import ai.zevaro.core.domain.rbac.RoleCategory;
import ai.zevaro.core.domain.rbac.RoleLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRoleRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull RoleCategory category,
        @NotNull RoleLevel level,
        List<String> permissionCodes
) {}
