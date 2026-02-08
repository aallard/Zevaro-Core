package ai.zevaro.core.domain.ticket.dto;

import ai.zevaro.core.domain.ticket.TicketResolution;
import ai.zevaro.core.domain.ticket.TicketSeverity;
import ai.zevaro.core.domain.ticket.TicketSource;
import ai.zevaro.core.domain.ticket.TicketStatus;
import ai.zevaro.core.domain.ticket.TicketType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    UUID workstreamId,
    String workstreamName,
    UUID programId,
    String programName,
    String identifier,
    String title,
    String description,
    TicketType type,
    TicketSeverity severity,
    TicketStatus status,
    TicketResolution resolution,
    UUID reportedById,
    String reportedByName,
    UUID assignedToId,
    String assignedToName,
    String environment,
    String stepsToReproduce,
    String expectedBehavior,
    String actualBehavior,
    TicketSource source,
    String externalRef,
    BigDecimal estimatedHours,
    BigDecimal actualHours,
    Instant resolvedAt,
    Instant closedAt,
    Instant createdAt,
    Instant updatedAt
) {}
