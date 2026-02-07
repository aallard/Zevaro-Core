package ai.zevaro.core.domain.stakeholder.dto;

import java.util.List;
import java.util.UUID;

public record StakeholderScorecardResponse(
    List<ScorecardEntry> stakeholders
) {
    public record ScorecardEntry(
        UUID stakeholderId,
        String name,
        String email,
        String avatarUrl,
        List<String> decisionDomains,
        double avgResponseTimeHours,
        double teamAvgResponseTimeHours,
        int decisionsPending,
        int decisionsCompletedThisMonth
    ) {}
}
