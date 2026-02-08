package ai.zevaro.core.domain.portfolio.dto;

import java.util.List;
import java.util.UUID;

public record PortfolioDashboardResponse(
        UUID portfolioId,
        String portfolioName,
        int totalPrograms,
        int activePrograms,
        int totalDecisionsPending,
        int totalDecisionsBreached,
        double avgDecisionCycleTimeHours,
        List<ProgramHealthSummary> programs
) {}
