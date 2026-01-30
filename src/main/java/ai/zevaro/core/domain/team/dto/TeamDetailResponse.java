package ai.zevaro.core.domain.team.dto;

import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TeamDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String iconUrl,
        String color,
        UserSummary lead,
        List<TeamMemberResponse> members,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
