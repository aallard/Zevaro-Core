package ai.zevaro.core.domain.outcome.dto;

import ai.zevaro.core.domain.outcome.OutcomePriority;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateOutcomeRequest(
        @Size(max = 500) String title,
        String description,
        String successCriteria,
        Map<String, Object> targetMetrics,
        Map<String, Object> currentMetrics,
        OutcomePriority priority,
        UUID teamId,
        UUID ownerId,
        LocalDate targetDate,
        List<String> tags,
        Map<String, String> externalRefs
) {}
