package ai.zevaro.core.domain.project.dto;

import ai.zevaro.core.domain.project.ProjectStatus;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String slug,
        String description,
        ProjectStatus status,
        String color,
        String iconUrl,
        UserSummary owner,
        int decisionCount,
        int outcomeCount,
        int hypothesisCount,
        int teamMemberCount,
        Instant createdAt,
        Instant updatedAt
) {}
