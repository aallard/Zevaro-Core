package ai.zevaro.core.domain.user.dto;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String fullName,
        String title,
        String avatarUrl
) {}
