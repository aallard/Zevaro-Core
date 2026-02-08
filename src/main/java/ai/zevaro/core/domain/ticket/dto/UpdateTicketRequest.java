package ai.zevaro.core.domain.ticket.dto;

import ai.zevaro.core.domain.ticket.TicketSeverity;
import ai.zevaro.core.domain.ticket.TicketType;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateTicketRequest(
    @Size(max = 500) String title,
    String description,
    TicketType type,
    TicketSeverity severity,
    UUID assignedToId,
    String environment,
    String stepsToReproduce,
    String expectedBehavior,
    String actualBehavior,
    String externalRef,
    BigDecimal estimatedHours,
    BigDecimal actualHours
) {}
