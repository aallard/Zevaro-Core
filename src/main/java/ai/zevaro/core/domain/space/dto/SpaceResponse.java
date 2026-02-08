package ai.zevaro.core.domain.space.dto;

import ai.zevaro.core.domain.space.SpaceStatus;
import ai.zevaro.core.domain.space.SpaceType;
import ai.zevaro.core.domain.space.SpaceVisibility;

import java.time.Instant;
import java.util.UUID;

public record SpaceResponse(
        UUID id,
        String name,
        String slug,
        String description,
        SpaceType type,
        SpaceStatus status,
        UUID programId,
        String programName,
        UUID ownerId,
        String ownerName,
        String icon,
        SpaceVisibility visibility,
        Integer sortOrder,
        int documentCount,
        Instant createdAt,
        Instant updatedAt
) {}
