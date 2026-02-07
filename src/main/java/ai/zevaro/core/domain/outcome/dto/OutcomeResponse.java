package ai.zevaro.core.domain.outcome.dto;

import ai.zevaro.core.domain.outcome.OutcomePriority;
import ai.zevaro.core.domain.outcome.OutcomeStatus;
import ai.zevaro.core.domain.project.dto.ProjectSummary;
import ai.zevaro.core.domain.team.dto.TeamSummary;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OutcomeResponse(
        UUID id,
        String title,
        String description,
        String successCriteria,
        Map<String, Object> targetMetrics,
        Map<String, Object> currentMetrics,
        OutcomeStatus status,
        OutcomePriority priority,
        TeamSummary team,
        UserSummary owner,
        ProjectSummary project,
        LocalDate targetDate,
        Instant startedAt,
        Instant validatedAt,
        UserSummary validatedBy,
        String validationNotes,
        Map<String, String> externalRefs,
        List<String> tags,
        int hypothesisCount,
        Instant createdAt,
        Instant updatedAt
) {}
