package ai.zevaro.core.domain.project.dto;

import ai.zevaro.core.domain.project.ProjectStatus;

import java.util.UUID;

public record ProjectSummary(
        UUID id,
        String name,
        String slug,
        ProjectStatus status,
        String color
) {}
