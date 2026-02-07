package ai.zevaro.core.domain.experiment.dto;

import ai.zevaro.core.domain.experiment.ExperimentStatus;
import ai.zevaro.core.domain.experiment.ExperimentType;
import ai.zevaro.core.domain.hypothesis.dto.HypothesisSummary;
import ai.zevaro.core.domain.project.dto.ProjectSummary;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ExperimentResponse(
        UUID id,
        String name,
        String description,
        ExperimentType type,
        ExperimentStatus status,
        Map<String, Object> config,
        Instant startDate,
        Instant endDate,
        Integer durationDays,
        Map<String, Object> results,
        String conclusion,
        String trafficSplit,
        String primaryMetric,
        List<String> secondaryMetrics,
        String audienceFilter,
        Integer sampleSizeTarget,
        Integer currentSampleSize,
        BigDecimal controlValue,
        BigDecimal variantValue,
        BigDecimal confidenceLevel,
        ProjectSummary project,
        HypothesisSummary hypothesis,
        UserSummary owner,
        Instant createdAt,
        Instant updatedAt
) {}
