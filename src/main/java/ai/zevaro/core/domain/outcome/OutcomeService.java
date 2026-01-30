package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.outcome.dto.CreateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.InvalidateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.OutcomeResponse;
import ai.zevaro.core.domain.outcome.dto.UpdateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.ValidateOutcomeRequest;
import ai.zevaro.core.domain.team.Team;
import ai.zevaro.core.domain.team.TeamRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutcomeService {

    private final OutcomeRepository outcomeRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final OutcomeMapper outcomeMapper;
    private final ObjectMapper objectMapper;

    private static final Set<OutcomeStatus> TERMINAL_STATUSES = Set.of(
            OutcomeStatus.VALIDATED,
            OutcomeStatus.INVALIDATED,
            OutcomeStatus.ABANDONED
    );

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomes(UUID tenantId, OutcomeStatus status, UUID teamId, OutcomePriority priority) {
        List<Outcome> outcomes;

        if (status != null) {
            outcomes = outcomeRepository.findByTenantIdAndStatus(tenantId, status);
        } else if (teamId != null) {
            outcomes = outcomeRepository.findByTenantIdAndTeamId(tenantId, teamId);
        } else if (priority != null) {
            outcomes = outcomeRepository.findByTenantIdAndPriority(tenantId, priority);
        } else {
            outcomes = outcomeRepository.findByTenantId(tenantId);
        }

        return outcomes.stream()
                .map(o -> outcomeMapper.toResponse(o, 0))
                .toList();
    }

    @Transactional(readOnly = true)
    public OutcomeResponse getOutcomeById(UUID id, UUID tenantId) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomesForTeam(UUID teamId, UUID tenantId) {
        return outcomeRepository.findByTenantIdAndTeamId(tenantId, teamId).stream()
                .map(o -> outcomeMapper.toResponse(o, 0))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomesForOwner(UUID ownerId, UUID tenantId) {
        return outcomeRepository.findByTenantIdAndOwnerId(tenantId, ownerId).stream()
                .map(o -> outcomeMapper.toResponse(o, 0))
                .toList();
    }

    @Transactional
    public OutcomeResponse createOutcome(UUID tenantId, CreateOutcomeRequest request, UUID createdById) {
        Outcome outcome = outcomeMapper.toEntity(request, tenantId, createdById);

        if (request.teamId() != null) {
            Team team = teamRepository.findByIdAndTenantId(request.teamId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.teamId()));
            outcome.setTeam(team);
        }

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            outcome.setOwner(owner);
        }

        outcome = outcomeRepository.save(outcome);
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional
    public OutcomeResponse updateOutcome(UUID id, UUID tenantId, UpdateOutcomeRequest request) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));

        outcomeMapper.updateEntity(outcome, request);

        if (request.teamId() != null) {
            Team team = teamRepository.findByIdAndTenantId(request.teamId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.teamId()));
            outcome.setTeam(team);
        }

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            outcome.setOwner(owner);
        }

        outcome = outcomeRepository.save(outcome);
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional
    public void deleteOutcome(UUID id, UUID tenantId) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));
        outcomeRepository.delete(outcome);
    }

    @Transactional
    public OutcomeResponse startOutcome(UUID id, UUID tenantId) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));

        if (outcome.getStatus() != OutcomeStatus.DRAFT && outcome.getStatus() != OutcomeStatus.NOT_STARTED) {
            throw new IllegalStateException("Can only start outcomes in DRAFT or NOT_STARTED status");
        }

        outcome.setStatus(OutcomeStatus.IN_PROGRESS);
        outcome.setStartedAt(Instant.now());
        outcome = outcomeRepository.save(outcome);
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional
    public OutcomeResponse validateOutcome(UUID id, UUID tenantId, ValidateOutcomeRequest request, UUID validatedById) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));

        if (TERMINAL_STATUSES.contains(outcome.getStatus())) {
            throw new IllegalStateException("Cannot validate an outcome in terminal status: " + outcome.getStatus());
        }

        User validatedBy = userRepository.findByIdAndTenantId(validatedById, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", validatedById));

        outcome.setStatus(OutcomeStatus.VALIDATED);
        outcome.setValidatedAt(Instant.now());
        outcome.setValidatedBy(validatedBy);
        outcome.setValidationNotes(request.validationNotes());

        if (request.finalMetrics() != null) {
            try {
                outcome.setCurrentMetrics(objectMapper.writeValueAsString(request.finalMetrics()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid metrics format", e);
            }
        }

        outcome = outcomeRepository.save(outcome);
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional
    public OutcomeResponse invalidateOutcome(UUID id, UUID tenantId, InvalidateOutcomeRequest request, UUID invalidatedById) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));

        if (TERMINAL_STATUSES.contains(outcome.getStatus())) {
            throw new IllegalStateException("Cannot invalidate an outcome in terminal status: " + outcome.getStatus());
        }

        User invalidatedBy = userRepository.findByIdAndTenantId(invalidatedById, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", invalidatedById));

        outcome.setStatus(OutcomeStatus.INVALIDATED);
        outcome.setInvalidatedAt(Instant.now());
        outcome.setValidatedBy(invalidatedBy);
        outcome.setValidationNotes(request.reason());

        if (request.finalMetrics() != null) {
            try {
                outcome.setCurrentMetrics(objectMapper.writeValueAsString(request.finalMetrics()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid metrics format", e);
            }
        }

        outcome = outcomeRepository.save(outcome);
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional
    public OutcomeResponse abandonOutcome(UUID id, UUID tenantId, String reason) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));

        if (TERMINAL_STATUSES.contains(outcome.getStatus())) {
            throw new IllegalStateException("Cannot abandon an outcome in terminal status: " + outcome.getStatus());
        }

        outcome.setStatus(OutcomeStatus.ABANDONED);
        outcome.setValidationNotes(reason);
        outcome = outcomeRepository.save(outcome);
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional
    public OutcomeResponse updateMetrics(UUID id, UUID tenantId, Map<String, Object> currentMetrics) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));

        try {
            outcome.setCurrentMetrics(objectMapper.writeValueAsString(currentMetrics));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid metrics format", e);
        }

        outcome = outcomeRepository.save(outcome);
        return outcomeMapper.toResponse(outcome, 0);
    }

    @Transactional(readOnly = true)
    public Map<OutcomeStatus, Long> getStatusCounts(UUID tenantId) {
        List<Object[]> results = outcomeRepository.countByStatusForTenant(tenantId);
        Map<OutcomeStatus, Long> counts = new EnumMap<>(OutcomeStatus.class);

        for (OutcomeStatus status : OutcomeStatus.values()) {
            counts.put(status, 0L);
        }

        for (Object[] result : results) {
            OutcomeStatus status = (OutcomeStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status, count);
        }

        return counts;
    }

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOverdueOutcomes(UUID tenantId) {
        return outcomeRepository.findOverdueOutcomes(tenantId, LocalDate.now()).stream()
                .map(o -> outcomeMapper.toResponse(o, 0))
                .toList();
    }
}
