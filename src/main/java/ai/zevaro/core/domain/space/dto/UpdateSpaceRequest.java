package ai.zevaro.core.domain.space.dto;

import ai.zevaro.core.domain.space.SpaceStatus;
import ai.zevaro.core.domain.space.SpaceVisibility;
import jakarta.validation.constraints.Size;

public record UpdateSpaceRequest(
        @Size(max = 255) String name,
        String description,
        SpaceStatus status,
        SpaceVisibility visibility,
        String icon,
        Integer sortOrder
) {}
