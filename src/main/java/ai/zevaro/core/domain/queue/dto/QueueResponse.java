package ai.zevaro.core.domain.queue.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record QueueResponse(
        UUID id,
        String name,
        String description,
        boolean isDefault,
        Map<String, Integer> slaConfig,
        Instant createdAt,
        Instant updatedAt
) {}
