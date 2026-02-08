package ai.zevaro.core.event.specification;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SpecificationStatusChangedEvent extends BaseEvent {
    private final UUID specificationId;
    private final String oldStatus;
    private final String newStatus;

    public SpecificationStatusChangedEvent(UUID tenantId, UUID actorId, UUID specificationId,
                                            String oldStatus, String newStatus) {
        super(tenantId, actorId);
        this.specificationId = specificationId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "specification.status-changed";
    }
}
