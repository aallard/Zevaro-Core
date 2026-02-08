package ai.zevaro.core.event.workstream;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class WorkstreamCreatedEvent extends BaseEvent {
    private final UUID workstreamId;
    private final UUID programId;
    private final String name;
    private final String mode;
    private final String executionMode;

    public WorkstreamCreatedEvent(UUID tenantId, UUID actorId, UUID workstreamId, UUID programId,
                                   String name, String mode, String executionMode) {
        super(tenantId, actorId);
        this.workstreamId = workstreamId;
        this.programId = programId;
        this.name = name;
        this.mode = mode;
        this.executionMode = executionMode;
    }

    @Override
    public String getEventType() {
        return "workstream.created";
    }
}
