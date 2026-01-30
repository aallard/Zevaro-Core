package ai.zevaro.core.domain.decision.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EscalateDecisionRequest(
        @NotNull UUID escalateToId,
        String reason
) {}
