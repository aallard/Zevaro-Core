package ai.zevaro.core.event.ticket;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TicketCreatedEvent extends BaseEvent {
    private final UUID ticketId;
    private final UUID workstreamId;
    private final String type;
    private final String severity;

    public TicketCreatedEvent(UUID tenantId, UUID actorId, UUID ticketId, UUID workstreamId,
                               String type, String severity) {
        super(tenantId, actorId);
        this.ticketId = ticketId;
        this.workstreamId = workstreamId;
        this.type = type;
        this.severity = severity;
    }

    @Override
    public String getEventType() {
        return "ticket.created";
    }
}
