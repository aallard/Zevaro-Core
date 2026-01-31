package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.outcome.dto.CreateKeyResultRequest;
import ai.zevaro.core.domain.outcome.dto.KeyResultResponse;
import ai.zevaro.core.domain.outcome.dto.UpdateKeyResultRequest;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyResultService {

    private final KeyResultRepository keyResultRepository;
    private final OutcomeRepository outcomeRepository;
    private final KeyResultMapper keyResultMapper;

    @Transactional(readOnly = true)
    public List<KeyResultResponse> getKeyResultsForOutcome(UUID outcomeId, UUID tenantId) {
        outcomeRepository.findByIdAndTenantId(outcomeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", outcomeId));

        return keyResultRepository.findByOutcomeIdOrderByCreatedAtAsc(outcomeId).stream()
                .map(keyResultMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KeyResultResponse getKeyResultById(UUID id, UUID outcomeId, UUID tenantId) {
        outcomeRepository.findByIdAndTenantId(outcomeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", outcomeId));

        KeyResult keyResult = keyResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KeyResult", "id", id));

        if (!keyResult.getOutcome().getId().equals(outcomeId)) {
            throw new ResourceNotFoundException("KeyResult", "id", id);
        }

        return keyResultMapper.toResponse(keyResult);
    }

    @Transactional
    public KeyResultResponse createKeyResult(UUID outcomeId, UUID tenantId, CreateKeyResultRequest request) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(outcomeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", outcomeId));

        KeyResult keyResult = keyResultMapper.toEntity(request, outcome);
        keyResult = keyResultRepository.save(keyResult);

        return keyResultMapper.toResponse(keyResult);
    }

    @Transactional
    public KeyResultResponse updateKeyResult(UUID id, UUID outcomeId, UUID tenantId, UpdateKeyResultRequest request) {
        outcomeRepository.findByIdAndTenantId(outcomeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", outcomeId));

        KeyResult keyResult = keyResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KeyResult", "id", id));

        if (!keyResult.getOutcome().getId().equals(outcomeId)) {
            throw new ResourceNotFoundException("KeyResult", "id", id);
        }

        keyResultMapper.updateEntity(keyResult, request);
        keyResult = keyResultRepository.save(keyResult);

        return keyResultMapper.toResponse(keyResult);
    }

    @Transactional
    public KeyResultResponse updateProgress(UUID id, UUID outcomeId, UUID tenantId, BigDecimal currentValue) {
        outcomeRepository.findByIdAndTenantId(outcomeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", outcomeId));

        KeyResult keyResult = keyResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KeyResult", "id", id));

        if (!keyResult.getOutcome().getId().equals(outcomeId)) {
            throw new ResourceNotFoundException("KeyResult", "id", id);
        }

        keyResult.setCurrentValue(currentValue);
        keyResult = keyResultRepository.save(keyResult);

        return keyResultMapper.toResponse(keyResult);
    }

    @Transactional
    public void deleteKeyResult(UUID id, UUID outcomeId, UUID tenantId) {
        outcomeRepository.findByIdAndTenantId(outcomeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", outcomeId));

        KeyResult keyResult = keyResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KeyResult", "id", id));

        if (!keyResult.getOutcome().getId().equals(outcomeId)) {
            throw new ResourceNotFoundException("KeyResult", "id", id);
        }

        keyResultRepository.delete(keyResult);
    }
}
