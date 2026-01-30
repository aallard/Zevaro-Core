package ai.zevaro.core.domain.team.dto;

import ai.zevaro.core.domain.team.TeamMemberRole;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record TeamMemberResponse(
        UUID id,
        UserSummary user,
        TeamMemberRole teamRole,
        Instant joinedAt
) {}
