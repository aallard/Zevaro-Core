package ai.zevaro.core.event.ticket;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TicketAssignedEvent extends BaseEvent {
    private final UUID ticketId;
    private final UUID assignedToId;

    public TicketAssignedEvent(UUID tenantId, UUID actorId, UUID ticketId, UUID assignedToId) {
        super(tenantId, actorId);
        this.ticketId = ticketId;
        this.assignedToId = assignedToId;
    }

    @Override
    public String getEventType() {
        return "ticket.assigned";
    }
}
