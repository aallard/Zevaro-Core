package ai.zevaro.core.event.program;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProgramStatusChangedEvent extends BaseEvent {
    private final UUID programId;
    private final String oldStatus;
    private final String newStatus;

    public ProgramStatusChangedEvent(UUID tenantId, UUID actorId, UUID programId, String oldStatus, String newStatus) {
        super(tenantId, actorId);
        this.programId = programId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "program.status-changed";
    }
}
