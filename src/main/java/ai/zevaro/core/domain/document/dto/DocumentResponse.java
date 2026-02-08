package ai.zevaro.core.domain.document.dto;

import ai.zevaro.core.domain.document.DocumentStatus;
import ai.zevaro.core.domain.document.DocumentType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID spaceId,
        String spaceName,
        UUID parentDocumentId,
        String parentDocumentTitle,
        String title,
        String body,
        DocumentType type,
        DocumentStatus status,
        int version,
        UUID authorId,
        String authorName,
        UUID lastEditedById,
        String lastEditedByName,
        Instant publishedAt,
        List<String> tags,
        Integer sortOrder,
        int childCount,
        Instant createdAt,
        Instant updatedAt
) {}
