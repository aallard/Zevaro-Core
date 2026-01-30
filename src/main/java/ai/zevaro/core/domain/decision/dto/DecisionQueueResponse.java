package ai.zevaro.core.domain.decision.dto;

import java.util.List;

public record DecisionQueueResponse(
        List<DecisionResponse> needsInput,
        List<DecisionResponse> underDiscussion,
        List<DecisionResponse> decided,
        long totalPending,
        Double avgDecisionTimeHours
) {}
