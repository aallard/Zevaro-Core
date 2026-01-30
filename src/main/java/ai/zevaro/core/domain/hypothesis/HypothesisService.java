package ai.zevaro.core.domain.hypothesis;

import ai.zevaro.core.domain.hypothesis.dto.ConcludeHypothesisRequest;
import ai.zevaro.core.domain.hypothesis.dto.CreateHypothesisRequest;
import ai.zevaro.core.domain.hypothesis.dto.HypothesisResponse;
import ai.zevaro.core.domain.hypothesis.dto.TransitionHypothesisRequest;
import ai.zevaro.core.domain.hypothesis.dto.UpdateHypothesisRequest;
import ai.zevaro.core.domain.outcome.Outcome;
import ai.zevaro.core.domain.outcome.OutcomeRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HypothesisService {

    private final HypothesisRepository hypothesisRepository;
    private final OutcomeRepository outcomeRepository;
    private final UserRepository userRepository;
    private final HypothesisMapper hypothesisMapper;
    private final ObjectMapper objectMapper;

    private static final Set<HypothesisStatus> TERMINAL_STATUSES = Set.of(
            HypothesisStatus.VALIDATED,
            HypothesisStatus.INVALIDATED,
            HypothesisStatus.ABANDONED
    );

    private static final Map<HypothesisStatus, Set<HypothesisStatus>> VALID_TRANSITIONS = Map.of(
            HypothesisStatus.DRAFT, EnumSet.of(HypothesisStatus.READY, HypothesisStatus.ABANDONED),
            HypothesisStatus.READY, EnumSet.of(HypothesisStatus.BUILDING, HypothesisStatus.BLOCKED, HypothesisStatus.ABANDONED),
            HypothesisStatus.BLOCKED, EnumSet.of(HypothesisStatus.READY, HypothesisStatus.ABANDONED),
            HypothesisStatus.BUILDING, EnumSet.of(HypothesisStatus.DEPLOYED, HypothesisStatus.BLOCKED, HypothesisStatus.ABANDONED),
            HypothesisStatus.DEPLOYED, EnumSet.of(HypothesisStatus.MEASURING, HypothesisStatus.BLOCKED, HypothesisStatus.ABANDONED),
            HypothesisStatus.MEASURING, EnumSet.of(HypothesisStatus.VALIDATED, HypothesisStatus.INVALIDATED, HypothesisStatus.BLOCKED, HypothesisStatus.ABANDONED),
            HypothesisStatus.VALIDATED, EnumSet.noneOf(HypothesisStatus.class),
            HypothesisStatus.INVALIDATED, EnumSet.noneOf(HypothesisStatus.class),
            HypothesisStatus.ABANDONED, EnumSet.noneOf(HypothesisStatus.class)
    );

    @Transactional(readOnly = true)
    public List<HypothesisResponse> getHypotheses(UUID tenantId, HypothesisStatus status, UUID outcomeId, HypothesisPriority priority) {
        List<Hypothesis> hypotheses;

        if (status != null) {
            hypotheses = hypothesisRepository.findByTenantIdAndStatus(tenantId, status);
        } else if (outcomeId != null) {
            hypotheses = hypothesisRepository.findByTenantIdAndOutcomeId(tenantId, outcomeId);
        } else if (priority != null) {
            hypotheses = hypothesisRepository.findByTenantIdAndPriority(tenantId, priority);
        } else {
            hypotheses = hypothesisRepository.findByTenantId(tenantId);
        }

        return hypotheses.stream()
                .map(hypothesisMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HypothesisResponse getHypothesisById(UUID id, UUID tenantId) {
        Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", id));
        return hypothesisMapper.toResponse(hypothesis);
    }

    @Transactional(readOnly = true)
    public List<HypothesisResponse> getHypothesesForOutcome(UUID outcomeId, UUID tenantId) {
        return hypothesisRepository.findByTenantIdAndOutcomeId(tenantId, outcomeId).stream()
                .map(hypothesisMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HypothesisResponse> getHypothesesForOwner(UUID ownerId, UUID tenantId) {
        return hypothesisRepository.findByTenantIdAndOwnerId(tenantId, ownerId).stream()
                .map(hypothesisMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HypothesisResponse> getBlockedHypotheses(UUID tenantId) {
        return hypothesisRepository.findBlockedByTenantId(tenantId).stream()
                .map(hypothesisMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HypothesisResponse> getActiveHypotheses(UUID tenantId) {
        return hypothesisRepository.findActiveByTenantId(tenantId).stream()
                .map(hypothesisMapper::toResponse)
                .toList();
    }

    @Transactional
    public HypothesisResponse createHypothesis(UUID tenantId, CreateHypothesisRequest request, UUID createdById) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(request.outcomeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", request.outcomeId()));

        Hypothesis hypothesis = hypothesisMapper.toEntity(request, tenantId, createdById);
        hypothesis.setOutcome(outcome);

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            hypothesis.setOwner(owner);
        }

        hypothesis = hypothesisRepository.save(hypothesis);
        return hypothesisMapper.toResponse(hypothesis);
    }

    @Transactional
    public HypothesisResponse updateHypothesis(UUID id, UUID tenantId, UpdateHypothesisRequest request) {
        Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", id));

        hypothesisMapper.updateEntity(hypothesis, request);

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            hypothesis.setOwner(owner);
        }

        hypothesis = hypothesisRepository.save(hypothesis);
        return hypothesisMapper.toResponse(hypothesis);
    }

    @Transactional
    public void deleteHypothesis(UUID id, UUID tenantId) {
        Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", id));
        hypothesisRepository.delete(hypothesis);
    }

    @Transactional
    public HypothesisResponse transitionHypothesis(UUID id, UUID tenantId, TransitionHypothesisRequest request) {
        Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", id));

        HypothesisStatus currentStatus = hypothesis.getStatus();
        HypothesisStatus targetStatus = request.targetStatus();

        validateTransition(currentStatus, targetStatus);

        hypothesis.setStatus(targetStatus);
        updateTimestampsForTransition(hypothesis, targetStatus);

        if (targetStatus == HypothesisStatus.BLOCKED && request.reason() != null) {
            hypothesis.setBlockedReason(request.reason());
        } else if (currentStatus == HypothesisStatus.BLOCKED) {
            hypothesis.setBlockedReason(null);
        }

        hypothesis = hypothesisRepository.save(hypothesis);
        return hypothesisMapper.toResponse(hypothesis);
    }

    @Transactional
    public HypothesisResponse concludeHypothesis(UUID id, UUID tenantId, ConcludeHypothesisRequest request, UUID concludedById) {
        Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", id));

        if (request.conclusion() != HypothesisStatus.VALIDATED && request.conclusion() != HypothesisStatus.INVALIDATED) {
            throw new IllegalArgumentException("Conclusion must be VALIDATED or INVALIDATED");
        }

        validateTransition(hypothesis.getStatus(), request.conclusion());

        User concludedBy = userRepository.findByIdAndTenantId(concludedById, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", concludedById));

        hypothesis.setStatus(request.conclusion());
        hypothesis.setConclusionNotes(request.conclusionNotes());
        hypothesis.setConcludedAt(Instant.now());
        hypothesis.setConcludedBy(concludedBy);

        if (request.experimentResults() != null) {
            try {
                hypothesis.setExperimentResults(objectMapper.writeValueAsString(request.experimentResults()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid experiment results format", e);
            }
        }

        hypothesis = hypothesisRepository.save(hypothesis);
        return hypothesisMapper.toResponse(hypothesis);
    }

    @Transactional
    public HypothesisResponse abandonHypothesis(UUID id, UUID tenantId, String reason) {
        Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", id));

        if (TERMINAL_STATUSES.contains(hypothesis.getStatus())) {
            throw new IllegalStateException("Cannot abandon a hypothesis in terminal status: " + hypothesis.getStatus());
        }

        hypothesis.setStatus(HypothesisStatus.ABANDONED);
        hypothesis.setConclusionNotes(reason);
        hypothesis = hypothesisRepository.save(hypothesis);
        return hypothesisMapper.toResponse(hypothesis);
    }

    @Transactional(readOnly = true)
    public Map<HypothesisStatus, Long> getStatusCounts(UUID tenantId) {
        List<Object[]> results = hypothesisRepository.countByStatusForTenant(tenantId);
        Map<HypothesisStatus, Long> counts = new EnumMap<>(HypothesisStatus.class);

        for (HypothesisStatus status : HypothesisStatus.values()) {
            counts.put(status, 0L);
        }

        for (Object[] result : results) {
            HypothesisStatus status = (HypothesisStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status, count);
        }

        return counts;
    }

    @Transactional(readOnly = true)
    public Map<HypothesisStatus, Long> getStatusCountsForOutcome(UUID outcomeId) {
        List<Object[]> results = hypothesisRepository.countByStatusForOutcome(outcomeId);
        Map<HypothesisStatus, Long> counts = new EnumMap<>(HypothesisStatus.class);

        for (HypothesisStatus status : HypothesisStatus.values()) {
            counts.put(status, 0L);
        }

        for (Object[] result : results) {
            HypothesisStatus status = (HypothesisStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status, count);
        }

        return counts;
    }

    private void validateTransition(HypothesisStatus from, HypothesisStatus to) {
        Set<HypothesisStatus> validTargets = VALID_TRANSITIONS.get(from);
        if (validTargets == null || !validTargets.contains(to)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", from, to));
        }
    }

    private void updateTimestampsForTransition(Hypothesis hypothesis, HypothesisStatus targetStatus) {
        Instant now = Instant.now();
        switch (targetStatus) {
            case BUILDING -> hypothesis.setStartedAt(now);
            case DEPLOYED -> hypothesis.setDeployedAt(now);
            case MEASURING -> hypothesis.setMeasuringStartedAt(now);
            default -> { }
        }
    }
}
