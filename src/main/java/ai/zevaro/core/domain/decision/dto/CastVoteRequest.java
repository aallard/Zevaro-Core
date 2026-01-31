package ai.zevaro.core.domain.decision.dto;

import ai.zevaro.core.domain.decision.VoteType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CastVoteRequest(
        @NotNull(message = "Vote type is required")
        VoteType vote,

        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String comment
) {}
