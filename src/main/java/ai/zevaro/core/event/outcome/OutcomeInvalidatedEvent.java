package ai.zevaro.core.event.outcome;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OutcomeInvalidatedEvent extends BaseEvent {
    private final UUID outcomeId;
    private final String title;
    private final UUID invalidatedById;
    private final String reason;

    public OutcomeInvalidatedEvent(UUID tenantId, UUID actorId, UUID outcomeId, String title,
                                    UUID invalidatedById, String reason) {
        super(tenantId, actorId);
        this.outcomeId = outcomeId;
        this.title = title;
        this.invalidatedById = invalidatedById;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "outcome.invalidated";
    }
}
