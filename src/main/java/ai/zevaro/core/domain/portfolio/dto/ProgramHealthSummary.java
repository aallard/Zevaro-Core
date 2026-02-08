package ai.zevaro.core.domain.portfolio.dto;

import ai.zevaro.core.domain.program.ProgramStatus;

import java.util.UUID;

public record ProgramHealthSummary(
        UUID programId,
        String programName,
        ProgramStatus status,
        String healthIndicator,
        int pendingDecisions,
        int breachedDecisions,
        int workstreamCount
) {}
