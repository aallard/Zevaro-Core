package ai.zevaro.core.domain.document.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID id,
        UUID documentId,
        int version,
        String title,
        String body,
        UUID editedById,
        String editedByName,
        Instant createdAt
) {}
