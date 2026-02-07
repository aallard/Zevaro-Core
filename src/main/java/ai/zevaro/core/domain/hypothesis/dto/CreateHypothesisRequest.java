package ai.zevaro.core.domain.hypothesis.dto;

import ai.zevaro.core.domain.hypothesis.ConfidenceLevel;
import ai.zevaro.core.domain.hypothesis.HypothesisPriority;
import ai.zevaro.core.domain.hypothesis.TShirtSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateHypothesisRequest(
        @NotNull(message = "Outcome ID is required")
        UUID outcomeId,

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 500, message = "Title must be between 3 and 500 characters")
        String title,

        @NotBlank(message = "Belief statement is required")
        @Size(min = 10, max = 2000, message = "Belief must be between 10 and 2000 characters")
        String belief,

        @NotBlank(message = "Expected result is required")
        @Size(max = 2000, message = "Expected result cannot exceed 2000 characters")
        String expectedResult,

        @Size(max = 2000, message = "Measurement criteria cannot exceed 2000 characters")
        String measurementCriteria,

        HypothesisPriority priority,
        TShirtSize effort,
        TShirtSize impact,
        ConfidenceLevel confidence,
        UUID ownerId,
        UUID projectId,
        Map<String, Object> experimentConfig,

        @Size(max = 20, message = "Cannot have more than 20 tags")
        List<String> tags
) {}
