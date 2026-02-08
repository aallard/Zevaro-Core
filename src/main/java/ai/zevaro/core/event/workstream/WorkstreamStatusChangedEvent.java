package ai.zevaro.core.event.workstream;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class WorkstreamStatusChangedEvent extends BaseEvent {
    private final UUID workstreamId;
    private final String oldStatus;
    private final String newStatus;

    public WorkstreamStatusChangedEvent(UUID tenantId, UUID actorId, UUID workstreamId,
                                         String oldStatus, String newStatus) {
        super(tenantId, actorId);
        this.workstreamId = workstreamId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "workstream.status-changed";
    }
}
