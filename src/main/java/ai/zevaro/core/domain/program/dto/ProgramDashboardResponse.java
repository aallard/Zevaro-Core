package ai.zevaro.core.domain.program.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProgramDashboardResponse(
    // Metric cards
    int pendingDecisionCount,
    int slaBreachedDecisionCount,
    int activeOutcomeCount,
    double outcomeValidationPercentage,
    int runningExperimentCount,
    double avgDecisionTimeHours,
    double avgDecisionTimeTrend, // positive = getting slower, negative = getting faster

    // Decision queue (top 5 urgent)
    List<DecisionQueueItem> urgentDecisions,

    // Decision velocity (last 30 days)
    List<DailyMetric> decisionVelocity,

    // Outcome progress
    List<OutcomeProgressItem> outcomeProgress,

    // Recent activity
    List<ActivityItem> recentActivity
) {
    public record DecisionQueueItem(
        UUID id,
        String title,
        String priority,
        String assigneeName,
        String assigneeAvatarUrl,
        long waitingMinutes,
        boolean slaBreached
    ) {}

    public record DailyMetric(
        LocalDate date,
        int count,
        double avgHours
    ) {}

    public record OutcomeProgressItem(
        UUID id,
        String title,
        String status,
        double progressPercent,
        String color
    ) {}

    public record ActivityItem(
        String actorName,
        String actorAvatarUrl,
        String action,
        String entityType,
        String entityTitle,
        Instant timestamp
    ) {}
}
