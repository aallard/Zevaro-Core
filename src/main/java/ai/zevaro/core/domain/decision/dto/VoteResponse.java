package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.VoteType;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(
        UUID id,
        UserSummary user,
        VoteType vote,
        String comment,
        Instant createdAt
) {}
