package ai.zevaro.core.domain.rbac.dto;

import ai.zevaro.core.domain.rbac.RoleCategory;
import ai.zevaro.core.domain.rbac.RoleLevel;

import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String code,
        String name,
        String description,
        RoleCategory category,
        RoleLevel level,
        List<String> permissions
) {}
