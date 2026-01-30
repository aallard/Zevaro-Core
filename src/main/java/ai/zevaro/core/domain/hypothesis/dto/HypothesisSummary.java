package ai.zevaro.core.domain.hypothesis.dto;

import ai.zevaro.core.domain.hypothesis.HypothesisPriority;
import ai.zevaro.core.domain.hypothesis.HypothesisStatus;

import java.util.UUID;

public record HypothesisSummary(
        UUID id,
        String title,
        HypothesisStatus status,
        HypothesisPriority priority
) {}
