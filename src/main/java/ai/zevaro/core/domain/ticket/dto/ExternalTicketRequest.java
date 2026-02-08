package ai.zevaro.core.domain.ticket.dto;

import ai.zevaro.core.domain.ticket.TicketSeverity;
import ai.zevaro.core.domain.ticket.TicketSource;
import ai.zevaro.core.domain.ticket.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ExternalTicketRequest(
    @NotNull UUID tenantId,
    @NotNull UUID workstreamId,
    @NotBlank String title,
    String description,
    @NotNull TicketType type,
    TicketSeverity severity,
    String environment,
    String externalRef,
    TicketSource source
) {}
