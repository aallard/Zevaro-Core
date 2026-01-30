package ai.zevaro.core.domain.stakeholder.dto;

import java.util.List;

public record StakeholderLeaderboard(
        List<StakeholderMetrics> fastestResponders,
        List<StakeholderMetrics> mostActive,
        List<StakeholderMetrics> needingAttention
) {}
