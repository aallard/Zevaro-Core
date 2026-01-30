package ai.zevaro.core.domain.team.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTeamRequest(
        @Size(max = 200) String name,
        String description,
        String iconUrl,
        String color,
        UUID leadId,
        Boolean active
) {}
