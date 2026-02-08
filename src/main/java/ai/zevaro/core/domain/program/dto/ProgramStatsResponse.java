package ai.zevaro.core.domain.program.dto;

public record ProgramStatsResponse(
        int pendingDecisionCount,
        int activeOutcomeCount,
        int runningExperimentCount,
        int totalHypothesisCount
) {}
