package ai.zevaro.core.domain.experiment.dto;

import ai.zevaro.core.domain.experiment.ExperimentType;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateExperimentRequest(
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        ExperimentType type,

        UUID projectId,

        UUID hypothesisId,

        UUID ownerId,

        Integer durationDays,

        String trafficSplit,

        String primaryMetric,

        List<String> secondaryMetrics,

        String audienceFilter,

        Integer sampleSizeTarget
) {}
