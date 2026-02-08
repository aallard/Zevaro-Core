package ai.zevaro.core.domain.program.dto;

import ai.zevaro.core.domain.program.ProgramStatus;
import ai.zevaro.core.domain.program.ProgramType;
import ai.zevaro.core.domain.user.dto.UserSummary;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProgramResponse(
        UUID id,
        String name,
        String slug,
        String description,
        ProgramStatus status,
        ProgramType type,
        UUID portfolioId,
        String color,
        String iconUrl,
        UserSummary owner,
        LocalDate startDate,
        LocalDate targetDate,
        List<String> tags,
        int decisionCount,
        int outcomeCount,
        int hypothesisCount,
        int teamMemberCount,
        Instant createdAt,
        Instant updatedAt
) {}
