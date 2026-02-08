package ai.zevaro.core.domain.specification.dto;

import ai.zevaro.core.domain.specification.SpecificationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SpecificationResponse(
    UUID id,
    UUID workstreamId,
    String workstreamName,
    UUID programId,
    String programName,
    String name,
    String description,
    UUID documentId,
    SpecificationStatus status,
    int version,
    UUID authorId,
    String authorName,
    UUID reviewerId,
    String reviewerName,
    Instant approvedAt,
    UUID approvedById,
    String approvedByName,
    BigDecimal estimatedHours,
    BigDecimal actualHours,
    int requirementCount,
    Instant createdAt,
    Instant updatedAt
) {}
