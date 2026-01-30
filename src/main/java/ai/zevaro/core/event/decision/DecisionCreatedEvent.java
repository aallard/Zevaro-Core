package ai.zevaro.core.event.decision;

import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.domain.decision.DecisionType;
import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class DecisionCreatedEvent extends BaseEvent {
    private final UUID decisionId;
    private final String title;
    private final DecisionPriority priority;
    private final DecisionType type;
    private final UUID assignedToId;
    private final UUID outcomeId;
    private final UUID hypothesisId;
    private final Instant dueAt;

    public DecisionCreatedEvent(UUID tenantId, UUID actorId, UUID decisionId, String title,
                                 DecisionPriority priority, DecisionType type, UUID assignedToId,
                                 UUID outcomeId, UUID hypothesisId, Instant dueAt) {
        super(tenantId, actorId);
        this.decisionId = decisionId;
        this.title = title;
        this.priority = priority;
        this.type = type;
        this.assignedToId = assignedToId;
        this.outcomeId = outcomeId;
        this.hypothesisId = hypothesisId;
        this.dueAt = dueAt;
    }

    @Override
    public String getEventType() {
        return "decision.created";
    }
}
