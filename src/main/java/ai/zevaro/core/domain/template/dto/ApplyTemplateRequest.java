package ai.zevaro.core.domain.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ApplyTemplateRequest(
        @NotBlank @Size(max = 255) String programName,
        String programDescription,
        UUID portfolioId,
        UUID ownerId
) {}
