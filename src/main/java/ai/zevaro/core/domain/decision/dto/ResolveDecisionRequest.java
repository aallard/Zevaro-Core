package ai.zevaro.core.domain.decision.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveDecisionRequest(
        @NotBlank String rationale,
        DecisionOption selectedOption,
        String selectedOptionId
) {}
