package ai.zevaro.core.domain.user.dto;

import ai.zevaro.core.domain.rbac.dto.RoleResponse;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        String title,
        String department,
        String avatarUrl,
        RoleResponse role,
        UserSummary manager,
        boolean active,
        Instant lastLoginAt,
        Instant createdAt
) {}
