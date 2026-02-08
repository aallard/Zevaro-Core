package ai.zevaro.core.domain.link.dto;

import ai.zevaro.core.domain.link.EntityType;
import ai.zevaro.core.domain.link.LinkType;

import java.time.Instant;
import java.util.UUID;

public record EntityLinkResponse(
        UUID id,
        EntityType sourceType,
        UUID sourceId,
        String sourceTitle,
        EntityType targetType,
        UUID targetId,
        String targetTitle,
        LinkType linkType,
        UUID createdById,
        String createdByName,
        Instant createdAt
) {}
