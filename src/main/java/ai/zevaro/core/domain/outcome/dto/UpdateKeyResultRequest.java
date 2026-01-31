package ai.zevaro.core.domain.outcome.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateKeyResultRequest(
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @Positive(message = "Target value must be positive")
        BigDecimal targetValue,

        BigDecimal currentValue,

        @Size(max = 50, message = "Unit cannot exceed 50 characters")
        String unit
) {}
