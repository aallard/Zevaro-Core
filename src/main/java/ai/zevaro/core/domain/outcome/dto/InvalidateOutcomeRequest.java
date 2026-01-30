package ai.zevaro.core.domain.outcome.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record InvalidateOutcomeRequest(
        @NotBlank String reason,
        Map<String, Object> finalMetrics
) {}
