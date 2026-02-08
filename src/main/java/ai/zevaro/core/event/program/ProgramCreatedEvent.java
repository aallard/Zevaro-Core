package ai.zevaro.core.event.program;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProgramCreatedEvent extends BaseEvent {
    private final UUID programId;
    private final String name;
    private final String status;

    public ProgramCreatedEvent(UUID tenantId, UUID actorId, UUID programId, String name, String status) {
        super(tenantId, actorId);
        this.programId = programId;
        this.name = name;
        this.status = status;
    }

    @Override
    public String getEventType() {
        return "program.created";
    }
}
