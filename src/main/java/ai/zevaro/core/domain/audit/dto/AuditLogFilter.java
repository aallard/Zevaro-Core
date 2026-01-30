package ai.zevaro.core.domain.audit.dto;

import ai.zevaro.core.domain.audit.AuditAction;

import java.time.Instant;
import java.util.UUID;

public record AuditLogFilter(
        UUID actorId,
        String entityType,
        UUID entityId,
        AuditAction action,
        Instant startDate,
        Instant endDate
) {}
