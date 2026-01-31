package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.VoteType;

import java.util.List;
import java.util.Map;

public record VoteSummary(
        int totalVotes,
        Map<VoteType, Long> countByType,
        List<VoteResponse> votes
) {}
