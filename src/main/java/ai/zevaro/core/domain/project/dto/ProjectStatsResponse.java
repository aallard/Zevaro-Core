package ai.zevaro.core.domain.project.dto;

public record ProjectStatsResponse(
        int pendingDecisionCount,
        int activeOutcomeCount,
        int runningExperimentCount,
        int totalHypothesisCount
) {}
