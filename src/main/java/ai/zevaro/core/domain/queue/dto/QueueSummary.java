package ai.zevaro.core.domain.queue.dto;

import java.util.UUID;

public record QueueSummary(
        UUID id,
        String name
) {}
