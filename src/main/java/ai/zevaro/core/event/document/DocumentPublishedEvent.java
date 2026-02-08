package ai.zevaro.core.event.document;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DocumentPublishedEvent extends BaseEvent {
    private final UUID documentId;
    private final UUID spaceId;
    private final String title;
    private final int version;

    public DocumentPublishedEvent(UUID tenantId, UUID actorId, UUID documentId, UUID spaceId,
                                   String title, int version) {
        super(tenantId, actorId);
        this.documentId = documentId;
        this.spaceId = spaceId;
        this.title = title;
        this.version = version;
    }

    @Override
    public String getEventType() {
        return "document.published";
    }
}
