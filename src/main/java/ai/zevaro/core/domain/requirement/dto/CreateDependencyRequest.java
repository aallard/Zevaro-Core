package ai.zevaro.core.domain.requirement.dto;

import ai.zevaro.core.domain.requirement.DependencyType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDependencyRequest(
    @NotNull UUID dependsOnId,
    @NotNull DependencyType type
) {}
