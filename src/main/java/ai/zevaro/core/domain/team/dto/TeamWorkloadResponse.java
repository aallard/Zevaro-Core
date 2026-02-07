package ai.zevaro.core.domain.team.dto;

import java.util.List;
import java.util.UUID;

public record TeamWorkloadResponse(
    UUID teamId,
    String teamName,
    int totalMembers,
    int totalStakeholders,
    int pendingDecisionsAcrossTeam,
    List<MemberWorkload> members
) {
    public record MemberWorkload(
        UUID userId,
        String name,
        String email,
        String avatarUrl,
        String role,
        int decisionsPending,
        double avgResponseTimeHours,
        int hypothesesOwned,
        boolean isActive
    ) {}
}
