package ai.zevaro.core.domain.requirement.dto;

import ai.zevaro.core.domain.requirement.RequirementPriority;
import ai.zevaro.core.domain.requirement.RequirementStatus;
import ai.zevaro.core.domain.requirement.RequirementType;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateRequirementRequest(
    @Size(max = 500) String title,
    String description,
    RequirementType type,
    RequirementPriority priority,
    RequirementStatus status,
    String acceptanceCriteria,
    BigDecimal estimatedHours,
    BigDecimal actualHours,
    Integer sortOrder
) {}
