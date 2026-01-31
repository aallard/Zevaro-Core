package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionType;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateDecisionRequest(
        @Size(max = 500) String title,
        String description,
        String context,
        List<DecisionOption> options,
        DecisionPriority priority,
        DecisionType decisionType,
        UUID ownerId,
        UUID assignedToId,
        UUID outcomeId,
        UUID hypothesisId,
        UUID teamId,
        UUID queueId,
        UUID stakeholderId,
        Integer slaHours,
        List<BlockedItem> blockedItems,
        Map<String, String> externalRefs,
        List<String> tags
) {}
