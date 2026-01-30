package ai.zevaro.core.domain.outcome.dto;

import ai.zevaro.core.domain.outcome.OutcomePriority;
import ai.zevaro.core.domain.outcome.OutcomeStatus;

import java.time.LocalDate;
import java.util.UUID;

public record OutcomeSummary(
        UUID id,
        String title,
        OutcomeStatus status,
        OutcomePriority priority,
        LocalDate targetDate
) {}
