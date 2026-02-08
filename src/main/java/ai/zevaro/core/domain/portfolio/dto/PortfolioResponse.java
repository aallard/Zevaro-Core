package ai.zevaro.core.domain.portfolio.dto;

import ai.zevaro.core.domain.portfolio.PortfolioStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioResponse(
        UUID id,
        String name,
        String slug,
        String description,
        PortfolioStatus status,
        UUID ownerId,
        String ownerName,
        List<String> tags,
        int programCount,
        Instant createdAt,
        Instant updatedAt
) {}
