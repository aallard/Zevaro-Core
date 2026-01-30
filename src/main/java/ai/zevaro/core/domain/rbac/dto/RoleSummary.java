package ai.zevaro.core.domain.rbac.dto;

import ai.zevaro.core.domain.rbac.RoleCategory;
import ai.zevaro.core.domain.rbac.RoleLevel;

import java.util.UUID;

public record RoleSummary(
        UUID id,
        String code,
        String name,
        RoleCategory category,
        RoleLevel level
) {}
