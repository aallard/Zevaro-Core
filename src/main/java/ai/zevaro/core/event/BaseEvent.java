package ai.zevaro.core.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant timestamp = Instant.now();
    private final UUID tenantId;
    private final UUID actorId;

    protected BaseEvent(UUID tenantId, UUID actorId) {
        this.tenantId = tenantId;
        this.actorId = actorId;
    }
}
