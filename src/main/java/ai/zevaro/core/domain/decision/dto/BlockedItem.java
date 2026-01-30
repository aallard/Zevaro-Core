package ai.zevaro.core.domain.decision.dto;

import java.util.UUID;

public record BlockedItem(
        String type,
        UUID id,
        String title
) {}
