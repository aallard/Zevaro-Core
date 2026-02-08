package ai.zevaro.core.event.comment;

import ai.zevaro.core.event.BaseEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CommentCreatedEvent extends BaseEvent {
    private final UUID commentId;
    private final String parentType;
    private final UUID parentId;

    public CommentCreatedEvent(UUID tenantId, UUID actorId, UUID commentId, String parentType, UUID parentId) {
        super(tenantId, actorId);
        this.commentId = commentId;
        this.parentType = parentType;
        this.parentId = parentId;
    }

    @Override
    public String getEventType() {
        return "comment.created";
    }
}
