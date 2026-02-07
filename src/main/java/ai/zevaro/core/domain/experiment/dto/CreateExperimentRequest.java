package ai.zevaro.core.domain.experiment.dto;

import ai.zevaro.core.domain.experiment.ExperimentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateExperimentRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
        String name,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @NotNull(message = "Type is required")
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
