package ai.zevaro.core.domain.workstream.dto;

import ai.zevaro.core.domain.workstream.ExecutionMode;
import ai.zevaro.core.domain.workstream.WorkstreamMode;
import ai.zevaro.core.domain.workstream.WorkstreamStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkstreamResponse(
    UUID id,
    UUID programId,
    String programName,
    String name,
    String description,
    WorkstreamMode mode,
    ExecutionMode executionMode,
    WorkstreamStatus status,
    UUID ownerId,
    String ownerName,
    Integer sortOrder,
    List<String> tags,
    int childEntityCount,
    Instant createdAt,
    Instant updatedAt
) {}
