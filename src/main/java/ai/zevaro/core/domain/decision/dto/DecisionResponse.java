package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionStatus;
import ai.zevaro.core.domain.decision.DecisionType;
import ai.zevaro.core.domain.hypothesis.dto.HypothesisSummary;
import ai.zevaro.core.domain.outcome.dto.OutcomeSummary;
import ai.zevaro.core.domain.queue.dto.QueueSummary;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderSummary;
import ai.zevaro.core.domain.team.dto.TeamSummary;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DecisionResponse(
        UUID id,
        String title,
        String description,
        String context,
        List<DecisionOption> options,
        DecisionStatus status,
        DecisionPriority priority,
        DecisionType decisionType,
        UserSummary owner,
        UserSummary assignedTo,
        OutcomeSummary outcome,
        HypothesisSummary hypothesis,
        TeamSummary team,
        QueueSummary queue,
        StakeholderSummary stakeholder,
        Integer slaHours,
        Instant dueAt,
        boolean overdue,
        long waitTimeHours,
        Integer escalationLevel,
        Instant escalatedAt,
        UserSummary escalatedTo,
        UserSummary decidedBy,
        Instant decidedAt,
        String decisionRationale,
        DecisionOption selectedOption,
        String resolution,
        boolean wasEscalated,
        List<BlockedItem> blockedItems,
        int commentCount,
        int voteCount,
        Map<String, String> externalRefs,
        List<String> tags,
        Instant createdAt,
        Instant updatedAt,
        List<VoteResponse> votes,
        List<CommentResponse> comments
) {}
