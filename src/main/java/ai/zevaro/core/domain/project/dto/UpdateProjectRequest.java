package ai.zevaro.core.domain.project.dto;

import ai.zevaro.core.domain.project.ProjectStatus;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProjectRequest(
        @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
        String name,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        ProjectStatus status,

        @Size(max = 7, message = "Color must be a valid hex color code")
        String color,

        UUID ownerId,

        String iconUrl
) {}
