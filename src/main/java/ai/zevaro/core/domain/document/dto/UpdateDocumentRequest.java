package ai.zevaro.core.domain.document.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateDocumentRequest(
        @Size(max = 500) String title,
        String body,
        List<String> tags,
        Integer sortOrder
) {}
