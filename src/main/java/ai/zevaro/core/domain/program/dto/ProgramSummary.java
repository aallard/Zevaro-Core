package ai.zevaro.core.domain.program.dto;

import ai.zevaro.core.domain.program.ProgramStatus;
import ai.zevaro.core.domain.program.ProgramType;

import java.util.UUID;

public record ProgramSummary(
        UUID id,
        String name,
        String slug,
        ProgramStatus status,
        ProgramType type,
        String color
) {}
