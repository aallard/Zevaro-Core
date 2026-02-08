package ai.zevaro.core.domain.document.dto;

import ai.zevaro.core.domain.document.DocumentStatus;
import ai.zevaro.core.domain.document.DocumentType;

import java.util.List;
import java.util.UUID;

public record DocumentTreeNode(
        UUID id,
        String title,
        DocumentType type,
        DocumentStatus status,
        Integer sortOrder,
        List<DocumentTreeNode> children
) {}
