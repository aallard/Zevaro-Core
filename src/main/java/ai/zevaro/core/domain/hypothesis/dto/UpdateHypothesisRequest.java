package ai.zevaro.core.domain.hypothesis.dto;

import ai.zevaro.core.domain.hypothesis.ConfidenceLevel;
import ai.zevaro.core.domain.hypothesis.HypothesisPriority;
import ai.zevaro.core.domain.hypothesis.TShirtSize;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateHypothesisRequest(
        @Size(max = 500) String title,
        String belief,
        String expectedResult,
        String measurementCriteria,
        HypothesisPriority priority,
        TShirtSize effort,
        TShirtSize impact,
        ConfidenceLevel confidence,
        UUID ownerId,
        Map<String, Object> experimentConfig,
        List<String> tags,
        Map<String, String> externalRefs
) {}
