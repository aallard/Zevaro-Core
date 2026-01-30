package ai.zevaro.core.event.decision;

import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DecisionEscalatedEvent extends BaseEvent {
    private final UUID decisionId;
    private final String title;
    private final DecisionPriority priority;
    private final UUID escalatedFromId;
    private final UUID escalatedToId;
    private final int escalationLevel;
    private final String reason;
    private final long waitTimeHours;

    public DecisionEscalatedEvent(UUID tenantId, UUID actorId, UUID decisionId, String title,
                                   DecisionPriority priority, UUID escalatedFromId, UUID escalatedToId,
                                   int escalationLevel, String reason, long waitTimeHours) {
        super(tenantId, actorId);
        this.decisionId = decisionId;
        this.title = title;
        this.priority = priority;
        this.escalatedFromId = escalatedFromId;
        this.escalatedToId = escalatedToId;
        this.escalationLevel = escalationLevel;
        this.reason = reason;
        this.waitTimeHours = waitTimeHours;
    }

    @Override
    public String getEventType() {
        return "decision.escalated";
    }
}
