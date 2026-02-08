package ai.zevaro.core.domain.portfolio.dto;

import ai.zevaro.core.domain.project.ProjectStatus;

import java.util.UUID;

public record ProgramHealthSummary(
        UUID programId,
        String programName,
        ProjectStatus status,
        String healthIndicator,
        int pendingDecisions,
        int breachedDecisions,
        int workstreamCount
) {}
