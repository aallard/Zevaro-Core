package ai.zevaro.core.domain.activity.dto;

import java.time.Instant;
import java.util.UUID;

public record ActivityEvent(
    UUID id,
    UUID actorId,
    String actorName,
    String action,
    String entityType,
    UUID entityId,
    String entityTitle,
    Instant timestamp,
    String details
) {}
