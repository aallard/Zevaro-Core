package ai.zevaro.core.domain.search.dto;

import java.util.UUID;

public record SearchResult(
        String entityType,
        UUID entityId,
        String title,
        String description,
        String status,
        UUID programId,
        String programName
) {}
