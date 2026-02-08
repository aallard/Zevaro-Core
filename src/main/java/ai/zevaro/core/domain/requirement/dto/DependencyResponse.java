package ai.zevaro.core.domain.requirement.dto;

import ai.zevaro.core.domain.requirement.DependencyType;

import java.time.Instant;
import java.util.UUID;

public record DependencyResponse(
    UUID id,
    UUID requirementId,
    String requirementIdentifier,
    String requirementTitle,
    UUID dependsOnId,
    String dependsOnIdentifier,
    String dependsOnTitle,
    DependencyType type,
    Instant createdAt
) {}
