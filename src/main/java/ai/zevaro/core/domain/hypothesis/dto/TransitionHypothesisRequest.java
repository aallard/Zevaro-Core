package ai.zevaro.core.domain.hypothesis.dto;

import ai.zevaro.core.domain.hypothesis.HypothesisStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionHypothesisRequest(
        @NotNull HypothesisStatus targetStatus,
        String reason
) {}
