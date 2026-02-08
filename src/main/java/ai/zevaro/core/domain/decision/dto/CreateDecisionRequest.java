package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.DecisionParentType;
import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateDecisionRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 500, message = "Title must be between 3 and 500 characters")
        String title,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        @Size(max = 5000, message = "Context cannot exceed 5000 characters")
        String context,

        List<DecisionOption> options,

        @NotNull(message = "Priority is required")
        DecisionPriority priority,

        DecisionType decisionType,
        UUID ownerId,
        UUID assignedToId,
        UUID outcomeId,
        UUID hypothesisId,
        UUID teamId,
        UUID projectId,
        UUID queueId,
        UUID stakeholderId,
        DecisionParentType parentType,
        UUID parentId,
        UUID workstreamId,

        @Min(value = 1, message = "SLA hours must be at least 1")
        @Max(value = 720, message = "SLA hours cannot exceed 720 (30 days)")
        Integer slaHours,

        List<BlockedItem> blockedItems,

        @Size(max = 20, message = "Cannot have more than 20 tags")
        List<String> tags
) {}
