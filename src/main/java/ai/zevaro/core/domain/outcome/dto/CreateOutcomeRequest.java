package ai.zevaro.core.domain.outcome.dto;

import ai.zevaro.core.domain.outcome.OutcomePriority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateOutcomeRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 500, message = "Title must be between 3 and 500 characters")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @NotBlank(message = "Success criteria is required")
        @Size(max = 2000, message = "Success criteria cannot exceed 2000 characters")
        String successCriteria,

        Map<String, Object> targetMetrics,

        OutcomePriority priority,
        UUID teamId,
        UUID ownerId,
        UUID projectId,
        UUID workstreamId,

        @Future(message = "Target date must be in the future")
        LocalDate targetDate,

        @Size(max = 20, message = "Cannot have more than 20 tags")
        List<String> tags
) {}
