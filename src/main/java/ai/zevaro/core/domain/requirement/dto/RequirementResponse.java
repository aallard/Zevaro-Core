package ai.zevaro.core.domain.requirement.dto;

import ai.zevaro.core.domain.requirement.RequirementPriority;
import ai.zevaro.core.domain.requirement.RequirementStatus;
import ai.zevaro.core.domain.requirement.RequirementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RequirementResponse(
    UUID id,
    UUID specificationId,
    String specificationName,
    UUID workstreamId,
    UUID programId,
    String identifier,
    String title,
    String description,
    RequirementType type,
    RequirementPriority priority,
    RequirementStatus status,
    String acceptanceCriteria,
    BigDecimal estimatedHours,
    BigDecimal actualHours,
    Integer sortOrder,
    List<DependencyResponse> dependencies,
    List<DependencyResponse> dependedOnBy,
    Instant createdAt,
    Instant updatedAt
) {}
