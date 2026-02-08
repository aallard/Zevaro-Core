package ai.zevaro.core.domain.space.dto;

import ai.zevaro.core.domain.space.SpaceType;
import ai.zevaro.core.domain.space.SpaceVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSpaceRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotNull SpaceType type,
        UUID programId,
        SpaceVisibility visibility,
        String icon,
        Integer sortOrder
) {}
