package ai.zevaro.core.event.ticket;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TicketResolvedEvent extends BaseEvent {
    private final UUID ticketId;
    private final String resolution;

    public TicketResolvedEvent(UUID tenantId, UUID actorId, UUID ticketId, String resolution) {
        super(tenantId, actorId);
        this.ticketId = ticketId;
        this.resolution = resolution;
    }

    @Override
    public String getEventType() {
        return "ticket.resolved";
    }
}
