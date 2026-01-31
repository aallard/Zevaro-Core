package ai.zevaro.core.domain.queue;

import ai.zevaro.core.domain.queue.dto.CreateQueueRequest;
import ai.zevaro.core.domain.queue.dto.QueueResponse;
import ai.zevaro.core.domain.queue.dto.UpdateQueueRequest;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DecisionQueueService {

    private final DecisionQueueRepository queueRepository;
    private final DecisionQueueMapper queueMapper;

    @Transactional(readOnly = true)
    public List<QueueResponse> getQueues(UUID tenantId) {
        return queueRepository.findByTenantId(tenantId).stream()
                .map(queueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QueueResponse getQueueById(UUID id, UUID tenantId) {
        DecisionQueue queue = queueRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DecisionQueue", "id", id));
        return queueMapper.toResponse(queue);
    }

    @Transactional(readOnly = true)
    public QueueResponse getDefaultQueue(UUID tenantId) {
        DecisionQueue queue = queueRepository.findByTenantIdAndIsDefaultTrue(tenantId)
                .orElse(null);
        return queue != null ? queueMapper.toResponse(queue) : null;
    }

    @Transactional
    public QueueResponse createQueue(UUID tenantId, CreateQueueRequest request) {
        if (queueRepository.existsByTenantIdAndName(tenantId, request.name())) {
            throw new IllegalArgumentException("Queue with name '" + request.name() + "' already exists");
        }

        DecisionQueue queue = queueMapper.toEntity(request, tenantId);

        if (Boolean.TRUE.equals(queue.getIsDefault())) {
            clearDefaultQueue(tenantId);
        }

        queue = queueRepository.save(queue);
        return queueMapper.toResponse(queue);
    }

    @Transactional
    public QueueResponse updateQueue(UUID id, UUID tenantId, UpdateQueueRequest request) {
        DecisionQueue queue = queueRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DecisionQueue", "id", id));

        if (request.name() != null && !request.name().equals(queue.getName())) {
            if (queueRepository.existsByTenantIdAndName(tenantId, request.name())) {
                throw new IllegalArgumentException("Queue with name '" + request.name() + "' already exists");
            }
        }

        if (Boolean.TRUE.equals(request.isDefault()) && !queue.getIsDefault()) {
            clearDefaultQueue(tenantId);
        }

        queueMapper.updateEntity(queue, request);
        queue = queueRepository.save(queue);
        return queueMapper.toResponse(queue);
    }

    @Transactional
    public void deleteQueue(UUID id, UUID tenantId) {
        DecisionQueue queue = queueRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DecisionQueue", "id", id));
        queueRepository.delete(queue);
    }

    @Transactional
    public QueueResponse setDefaultQueue(UUID id, UUID tenantId) {
        DecisionQueue queue = queueRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DecisionQueue", "id", id));

        clearDefaultQueue(tenantId);
        queue.setIsDefault(true);
        queue = queueRepository.save(queue);
        return queueMapper.toResponse(queue);
    }

    private void clearDefaultQueue(UUID tenantId) {
        queueRepository.findByTenantIdAndIsDefaultTrue(tenantId)
                .ifPresent(existingDefault -> {
                    existingDefault.setIsDefault(false);
                    queueRepository.save(existingDefault);
                });
    }
}
