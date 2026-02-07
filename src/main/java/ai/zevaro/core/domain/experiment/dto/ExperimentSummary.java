package ai.zevaro.core.domain.experiment.dto;

import ai.zevaro.core.domain.experiment.ExperimentStatus;
import ai.zevaro.core.domain.experiment.ExperimentType;

import java.math.BigDecimal;
import java.util.UUID;

public record ExperimentSummary(
        UUID id,
        String name,
        ExperimentType type,
        ExperimentStatus status,
        Integer durationDays,
        BigDecimal confidenceLevel
) {}
