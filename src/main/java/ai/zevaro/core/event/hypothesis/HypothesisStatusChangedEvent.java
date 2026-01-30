package ai.zevaro.core.event.hypothesis;

import ai.zevaro.core.domain.hypothesis.HypothesisStatus;
import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class HypothesisStatusChangedEvent extends BaseEvent {
    private final UUID hypothesisId;
    private final String title;
    private final HypothesisStatus previousStatus;
    private final HypothesisStatus newStatus;
    private final UUID outcomeId;
    private final UUID blockedByDecisionId;

    public HypothesisStatusChangedEvent(UUID tenantId, UUID actorId, UUID hypothesisId, String title,
                                         HypothesisStatus previousStatus, HypothesisStatus newStatus,
                                         UUID outcomeId, UUID blockedByDecisionId) {
        super(tenantId, actorId);
        this.hypothesisId = hypothesisId;
        this.title = title;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.outcomeId = outcomeId;
        this.blockedByDecisionId = blockedByDecisionId;
    }

    @Override
    public String getEventType() {
        return "hypothesis.status-changed";
    }
}
