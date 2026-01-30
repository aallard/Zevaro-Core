package ai.zevaro.core.domain.hypothesis.dto;

import ai.zevaro.core.domain.hypothesis.HypothesisPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateHypothesisRequest(
        @NotNull UUID outcomeId,
        @NotBlank @Size(max = 500) String title,
        @NotBlank String belief,
        @NotBlank String expectedResult,
        String measurementCriteria,
        HypothesisPriority priority,
        UUID ownerId,
        Map<String, Object> experimentConfig,
        List<String> tags
) {}
