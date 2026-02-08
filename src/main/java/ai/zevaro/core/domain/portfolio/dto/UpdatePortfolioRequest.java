package ai.zevaro.core.domain.portfolio.dto;

import ai.zevaro.core.domain.portfolio.PortfolioStatus;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdatePortfolioRequest(
        @Size(max = 255) String name,
        String description,
        PortfolioStatus status,
        UUID ownerId,
        List<String> tags
) {}
