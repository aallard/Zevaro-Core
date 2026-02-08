package ai.zevaro.core.domain.specification.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateSpecificationRequest(
    @Size(max = 255) String name,
    String description,
    UUID reviewerId,
    BigDecimal estimatedHours,
    BigDecimal actualHours
) {}
