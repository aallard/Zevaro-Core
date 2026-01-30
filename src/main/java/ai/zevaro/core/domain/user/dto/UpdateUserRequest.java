package ai.zevaro.core.domain.user.dto;

import java.util.UUID;

public record UpdateUserRequest(
        String name,
        String title,
        String department,
        String avatarUrl,
        UUID roleId,
        UUID managerId,
        Boolean active
) {}
