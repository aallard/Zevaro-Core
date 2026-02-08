package ai.zevaro.core.event.specification;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SpecificationApprovedEvent extends BaseEvent {
    private final UUID specificationId;

    public SpecificationApprovedEvent(UUID tenantId, UUID actorId, UUID specificationId) {
        super(tenantId, actorId);
        this.specificationId = specificationId;
    }

    @Override
    public String getEventType() {
        return "specification.approved";
    }
}
