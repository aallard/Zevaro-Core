package ai.zevaro.core.domain.outcome.dto;

import ai.zevaro.core.domain.outcome.OutcomePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateOutcomeRequest(
        @NotBlank @Size(max = 500) String title,
        String description,
        @NotBlank String successCriteria,
        Map<String, Object> targetMetrics,
        OutcomePriority priority,
        UUID teamId,
        UUID ownerId,
        LocalDate targetDate,
        List<String> tags
) {}
