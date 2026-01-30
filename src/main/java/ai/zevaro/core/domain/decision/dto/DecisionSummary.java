package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionStatus;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.util.UUID;

public record DecisionSummary(
        UUID id,
        String title,
        DecisionStatus status,
        DecisionPriority priority,
        long waitTimeHours,
        boolean overdue,
        UserSummary assignedTo
) {}
