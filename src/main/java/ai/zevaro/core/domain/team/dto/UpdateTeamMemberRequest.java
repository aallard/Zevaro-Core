package ai.zevaro.core.domain.team.dto;

import ai.zevaro.core.domain.team.TeamMemberRole;
import jakarta.validation.constraints.NotNull;

public record UpdateTeamMemberRequest(
        @NotNull TeamMemberRole role
) {}
