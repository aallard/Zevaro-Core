package ai.zevaro.core.domain.stakeholder.dto;

import ai.zevaro.core.domain.decision.dto.DecisionSummary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StakeholderMetrics(
        UUID stakeholderId,
        String name,
        Integer decisionsPending,
        Integer decisionsCompleted,
        Integer decisionsEscalated,
        Double avgResponseTimeHours,
        Double escalationRate,
        Instant lastDecisionAt,
        List<DecisionSummary> pendingDecisions
) {}
