package ai.zevaro.core.domain.queue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateQueueRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        Boolean isDefault,

        Map<String, Integer> slaConfig
) {}
