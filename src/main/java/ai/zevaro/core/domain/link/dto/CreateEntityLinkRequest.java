package ai.zevaro.core.domain.link.dto;

import ai.zevaro.core.domain.link.EntityType;
import ai.zevaro.core.domain.link.LinkType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEntityLinkRequest(
        @NotNull EntityType sourceType,
        @NotNull UUID sourceId,
        @NotNull EntityType targetType,
        @NotNull UUID targetId,
        @NotNull LinkType linkType
) {}
