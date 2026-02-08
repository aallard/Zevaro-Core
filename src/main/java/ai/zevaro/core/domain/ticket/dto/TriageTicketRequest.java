package ai.zevaro.core.domain.ticket.dto;

import ai.zevaro.core.domain.ticket.TicketSeverity;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TriageTicketRequest(
    @NotNull TicketSeverity severity,
    UUID assignedToId
) {}
