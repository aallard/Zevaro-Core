package ai.zevaro.core.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    String getEventType();
    UUID getTenantId();
    Instant getTimestamp();
    UUID getActorId();
}
