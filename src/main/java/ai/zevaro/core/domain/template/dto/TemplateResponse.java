package ai.zevaro.core.domain.template.dto;

import java.time.Instant;
import java.util.UUID;

public record TemplateResponse(
        UUID id,
        String name,
        String description,
        String structure,
        boolean isSystem,
        UUID createdById,
        Instant createdAt,
        Instant updatedAt
) {}
