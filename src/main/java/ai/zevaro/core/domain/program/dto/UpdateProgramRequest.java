package ai.zevaro.core.domain.program.dto;

import ai.zevaro.core.domain.program.ProgramStatus;
import ai.zevaro.core.domain.program.ProgramType;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateProgramRequest(
        @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
        String name,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        ProgramStatus status,

        @Size(max = 7, message = "Color must be a valid hex color code")
        String color,

        UUID ownerId,

        String iconUrl,

        ProgramType type,

        UUID portfolioId,

        LocalDate startDate,

        LocalDate targetDate,

        List<String> tags
) {}
