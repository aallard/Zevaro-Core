package ai.zevaro.core.event.outcome;

import ai.zevaro.core.domain.outcome.OutcomePriority;
import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OutcomeCreatedEvent extends BaseEvent {
    private final UUID outcomeId;
    private final String title;
    private final OutcomePriority priority;
    private final UUID teamId;
    private final UUID ownerId;

    public OutcomeCreatedEvent(UUID tenantId, UUID actorId, UUID outcomeId, String title,
                                OutcomePriority priority, UUID teamId, UUID ownerId) {
        super(tenantId, actorId);
        this.outcomeId = outcomeId;
        this.title = title;
        this.priority = priority;
        this.teamId = teamId;
        this.ownerId = ownerId;
    }

    @Override
    public String getEventType() {
        return "outcome.created";
    }
}
