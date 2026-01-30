package ai.zevaro.core.domain.team.dto;

import ai.zevaro.core.domain.team.TeamMemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddTeamMemberRequest(
        @NotNull UUID userId,
        TeamMemberRole role
) {}
