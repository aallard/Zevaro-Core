package ai.zevaro.core.domain.specification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSpecificationRequest(
    @NotBlank @Size(max = 255) String name,
    String description,
    UUID reviewerId,
    BigDecimal estimatedHours
) {}
