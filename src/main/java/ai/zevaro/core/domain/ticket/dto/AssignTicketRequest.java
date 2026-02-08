package ai.zevaro.core.domain.ticket.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTicketRequest(
    @NotNull UUID assignedToId
) {}
