package ai.zevaro.core.event.decision;

import ai.zevaro.core.domain.decision.DecisionPriority;
import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class DecisionResolvedEvent extends BaseEvent {
    private final UUID decisionId;
    private final String title;
    private final DecisionPriority priority;
    private final UUID decidedById;
    private final String rationale;
    private final long cycleTimeHours;
    private final boolean wasEscalated;
    private final int escalationLevel;
    private final List<UUID> unblockedHypothesisIds;

    public DecisionResolvedEvent(UUID tenantId, UUID actorId, UUID decisionId, String title,
                                  DecisionPriority priority, UUID decidedById, String rationale,
                                  long cycleTimeHours, boolean wasEscalated, int escalationLevel,
                                  List<UUID> unblockedHypothesisIds) {
        super(tenantId, actorId);
        this.decisionId = decisionId;
        this.title = title;
        this.priority = priority;
        this.decidedById = decidedById;
        this.rationale = rationale;
        this.cycleTimeHours = cycleTimeHours;
        this.wasEscalated = wasEscalated;
        this.escalationLevel = escalationLevel;
        this.unblockedHypothesisIds = unblockedHypothesisIds;
    }

    @Override
    public String getEventType() {
        return "decision.resolved";
    }
}
