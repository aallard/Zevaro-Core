package ai.zevaro.core.event.outcome;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class OutcomeValidatedEvent extends BaseEvent {
    private final UUID outcomeId;
    private final String title;
    private final UUID validatedById;
    private final String validationNotes;
    private final Map<String, Object> finalMetrics;
    private final long daysToValidation;

    public OutcomeValidatedEvent(UUID tenantId, UUID actorId, UUID outcomeId, String title,
                                  UUID validatedById, String validationNotes,
                                  Map<String, Object> finalMetrics, long daysToValidation) {
        super(tenantId, actorId);
        this.outcomeId = outcomeId;
        this.title = title;
        this.validatedById = validatedById;
        this.validationNotes = validationNotes;
        this.finalMetrics = finalMetrics;
        this.daysToValidation = daysToValidation;
    }

    @Override
    public String getEventType() {
        return "outcome.validated";
    }
}
