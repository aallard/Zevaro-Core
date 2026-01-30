package ai.zevaro.core.domain.user.dto;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String name,
        String title,
        String avatarUrl
) {}
