package ai.zevaro.core.domain.outcome.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record KeyResultResponse(
        UUID id,
        UUID outcomeId,
        String title,
        String description,
        BigDecimal targetValue,
        BigDecimal currentValue,
        String unit,
        BigDecimal progressPercent,
        Instant createdAt,
        Instant updatedAt
) {}
