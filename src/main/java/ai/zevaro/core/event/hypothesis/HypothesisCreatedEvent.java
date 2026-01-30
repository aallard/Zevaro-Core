package ai.zevaro.core.event.hypothesis;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class HypothesisCreatedEvent extends BaseEvent {
    private final UUID hypothesisId;
    private final String title;
    private final UUID outcomeId;
    private final UUID assigneeId;

    public HypothesisCreatedEvent(UUID tenantId, UUID actorId, UUID hypothesisId, String title,
                                   UUID outcomeId, UUID assigneeId) {
        super(tenantId, actorId);
        this.hypothesisId = hypothesisId;
        this.title = title;
        this.outcomeId = outcomeId;
        this.assigneeId = assigneeId;
    }

    @Override
    public String getEventType() {
        return "hypothesis.created";
    }
}
