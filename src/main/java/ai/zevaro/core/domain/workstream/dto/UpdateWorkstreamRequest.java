package ai.zevaro.core.domain.workstream.dto;

import ai.zevaro.core.domain.workstream.ExecutionMode;
import ai.zevaro.core.domain.workstream.WorkstreamStatus;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateWorkstreamRequest(
    @Size(max = 255) String name,
    String description,
    WorkstreamStatus status,
    ExecutionMode executionMode,
    UUID ownerId,
    Integer sortOrder,
    List<String> tags
) {}
