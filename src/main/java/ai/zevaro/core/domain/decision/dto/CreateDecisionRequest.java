package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateDecisionRequest(
        @NotBlank @Size(max = 500) String title,
        String description,
        String context,
        List<DecisionOption> options,
        @NotNull DecisionPriority priority,
        DecisionType decisionType,
        UUID ownerId,
        UUID assignedToId,
        UUID outcomeId,
        UUID hypothesisId,
        UUID teamId,
        Integer slaHours,
        List<BlockedItem> blockedItems,
        List<String> tags
) {}
