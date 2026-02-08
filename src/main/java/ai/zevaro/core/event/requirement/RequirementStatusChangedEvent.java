package ai.zevaro.core.event.requirement;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RequirementStatusChangedEvent extends BaseEvent {
    private final UUID requirementId;
    private final UUID specificationId;
    private final String oldStatus;
    private final String newStatus;

    public RequirementStatusChangedEvent(UUID tenantId, UUID actorId, UUID requirementId,
                                          UUID specificationId, String oldStatus, String newStatus) {
        super(tenantId, actorId);
        this.requirementId = requirementId;
        this.specificationId = specificationId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return "requirement.status-changed";
    }
}
