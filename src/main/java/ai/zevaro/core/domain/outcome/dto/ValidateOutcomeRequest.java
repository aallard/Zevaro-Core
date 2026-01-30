package ai.zevaro.core.domain.outcome.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ValidateOutcomeRequest(
        @NotBlank String validationNotes,
        Map<String, Object> finalMetrics
) {}
