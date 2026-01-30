package ai.zevaro.core.event.hypothesis;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class HypothesisConcludedEvent extends BaseEvent {
    private final UUID hypothesisId;
    private final String title;
    private final boolean validated;
    private final UUID outcomeId;
    private final Map<String, Object> experimentResults;
    private final long daysToConclusion;

    public HypothesisConcludedEvent(UUID tenantId, UUID actorId, UUID hypothesisId, String title,
                                     boolean validated, UUID outcomeId,
                                     Map<String, Object> experimentResults, long daysToConclusion) {
        super(tenantId, actorId);
        this.hypothesisId = hypothesisId;
        this.title = title;
        this.validated = validated;
        this.outcomeId = outcomeId;
        this.experimentResults = experimentResults;
        this.daysToConclusion = daysToConclusion;
    }

    @Override
    public String getEventType() {
        return "hypothesis.concluded";
    }
}
