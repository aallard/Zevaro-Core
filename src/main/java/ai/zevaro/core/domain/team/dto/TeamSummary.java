package ai.zevaro.core.domain.team.dto;

import java.util.UUID;

public record TeamSummary(
        UUID id,
        String name,
        String slug,
        String color
) {}
