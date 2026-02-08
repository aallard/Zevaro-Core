package ai.zevaro.core.domain.document.dto;

import ai.zevaro.core.domain.document.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateDocumentRequest(
        @NotNull UUID spaceId,
        UUID parentDocumentId,
        @NotBlank @Size(max = 500) String title,
        String body,
        @NotNull DocumentType type,
        List<String> tags,
        Integer sortOrder
) {}
