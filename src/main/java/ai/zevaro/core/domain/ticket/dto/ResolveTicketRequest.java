package ai.zevaro.core.domain.ticket.dto;

import ai.zevaro.core.domain.ticket.TicketResolution;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ResolveTicketRequest(
    @NotNull TicketResolution resolution,
    BigDecimal actualHours
) {}
