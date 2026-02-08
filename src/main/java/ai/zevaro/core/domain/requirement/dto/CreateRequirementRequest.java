package ai.zevaro.core.domain.requirement.dto;

import ai.zevaro.core.domain.requirement.RequirementPriority;
import ai.zevaro.core.domain.requirement.RequirementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateRequirementRequest(
    @NotBlank @Size(max = 500) String title,
    String description,
    @NotNull RequirementType type,
    @NotNull RequirementPriority priority,
    String acceptanceCriteria,
    BigDecimal estimatedHours,
    Integer sortOrder
) {}
