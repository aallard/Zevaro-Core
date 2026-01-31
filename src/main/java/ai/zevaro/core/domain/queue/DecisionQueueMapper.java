package ai.zevaro.core.domain.queue;

import ai.zevaro.core.domain.queue.dto.CreateQueueRequest;
import ai.zevaro.core.domain.queue.dto.QueueResponse;
import ai.zevaro.core.domain.queue.dto.QueueSummary;
import ai.zevaro.core.domain.queue.dto.UpdateQueueRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DecisionQueueMapper {

    public DecisionQueue toEntity(CreateQueueRequest request, UUID tenantId) {
        return DecisionQueue.builder()
                .tenantId(tenantId)
                .name(request.name())
                .description(request.description())
                .isDefault(request.isDefault() != null ? request.isDefault() : false)
                .slaConfig(request.slaConfig())
                .build();
    }

    public void updateEntity(DecisionQueue queue, UpdateQueueRequest request) {
        if (request.name() != null) {
            queue.setName(request.name());
        }
        if (request.description() != null) {
            queue.setDescription(request.description());
        }
        if (request.isDefault() != null) {
            queue.setIsDefault(request.isDefault());
        }
        if (request.slaConfig() != null) {
            queue.setSlaConfig(request.slaConfig());
        }
    }

    public QueueResponse toResponse(DecisionQueue queue) {
        if (queue == null) {
            return null;
        }
        return new QueueResponse(
                queue.getId(),
                queue.getName(),
                queue.getDescription(),
                queue.getIsDefault(),
                queue.getSlaConfig(),
                queue.getCreatedAt(),
                queue.getUpdatedAt()
        );
    }

    public QueueSummary toSummary(DecisionQueue queue) {
        if (queue == null) {
            return null;
        }
        return new QueueSummary(
                queue.getId(),
                queue.getName()
        );
    }
}
