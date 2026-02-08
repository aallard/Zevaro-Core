package ai.zevaro.core.domain.workstream.dto;

import ai.zevaro.core.domain.workstream.ExecutionMode;
import ai.zevaro.core.domain.workstream.WorkstreamMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateWorkstreamRequest(
    @NotBlank @Size(max = 255) String name,
    String description,
    @NotNull WorkstreamMode mode,
    @NotNull ExecutionMode executionMode,
    UUID ownerId,
    Integer sortOrder,
    List<String> tags
) {}
