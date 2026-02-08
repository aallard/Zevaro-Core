package ai.zevaro.core.event.specification;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SpecificationCreatedEvent extends BaseEvent {
    private final UUID specificationId;
    private final UUID workstreamId;
    private final UUID programId;
    private final String name;

    public SpecificationCreatedEvent(UUID tenantId, UUID actorId, UUID specificationId,
                                      UUID workstreamId, UUID programId, String name) {
        super(tenantId, actorId);
        this.specificationId = specificationId;
        this.workstreamId = workstreamId;
        this.programId = programId;
        this.name = name;
    }

    @Override
    public String getEventType() {
        return "specification.created";
    }
}
