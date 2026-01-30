package ai.zevaro.core.domain.hypothesis.dto;

import ai.zevaro.core.domain.hypothesis.HypothesisStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ConcludeHypothesisRequest(
        @NotNull HypothesisStatus conclusion,
        String conclusionNotes,
        Map<String, Object> experimentResults
) {}
