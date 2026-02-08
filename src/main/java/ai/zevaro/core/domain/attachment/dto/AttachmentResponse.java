package ai.zevaro.core.domain.attachment.dto;

import ai.zevaro.core.domain.attachment.AttachmentParentType;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        AttachmentParentType parentType,
        UUID parentId,
        String fileName,
        String fileType,
        Long fileSize,
        UUID uploadedById,
        String uploadedByName,
        Instant createdAt
) {}
