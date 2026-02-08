package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.outcome.dto.CreateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.InvalidateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.OutcomeResponse;
import ai.zevaro.core.domain.outcome.dto.UpdateOutcomeRequest;
import ai.zevaro.core.domain.outcome.dto.ValidateOutcomeRequest;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.team.Team;
import ai.zevaro.core.domain.team.TeamRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.event.EventPublisher;
import ai.zevaro.core.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final HypothesisRepository hypothesisRepository;
    private final TeamRepository teamRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final OutcomeMapper outcomeMapper;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    private static final Set<OutcomeStatus> TERMINAL_STATUSES = Set.of(
            OutcomeStatus.VALIDATED,
            OutcomeStatus.INVALIDATED,
            OutcomeStatus.ABANDONED
    );

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomes(UUID tenantId, OutcomeStatus status, UUID teamId, OutcomePriority priority, UUID projectId) {
        List<Outcome> outcomes;

        if (projectId != null) {
            outcomes = outcomeRepository.findByTenantIdAndProgramId(tenantId, projectId);
        } else if (status != null) {
            outcomes = outcomeRepository.findByTenantIdAndStatus(tenantId, status);
        } else if (teamId != null) {
            outcomes = outcomeRepository.findByTenantIdAndTeamId(tenantId, teamId);
        } else if (priority != null) {
            outcomes = outcomeRepository.findByTenantIdAndPriority(tenantId, priority);
        } else {
            outcomes = outcomeRepository.findByTenantId(tenantId);
        }

        return outcomes.stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OutcomeResponse> getOutcomesPaged(UUID tenantId, OutcomeStatus status, UUID teamId,
                                                   OutcomePriority priority, UUID projectId, Pageable pageable) {
        Page<Outcome> outcomes;

        if (projectId != null) {
            outcomes = outcomeRepository.findByTenantIdAndProgramId(tenantId, projectId, pageable);
        } else if (status != null) {
            outcomes = outcomeRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        } else if (teamId != null) {
            outcomes = outcomeRepository.findByTenantIdAndTeamId(tenantId, teamId, pageable);
        } else if (priority != null) {
            outcomes = outcomeRepository.findByTenantIdAndPriority(tenantId, priority, pageable);
        } else {
            outcomes = outcomeRepository.findByTenantId(tenantId, pageable);
        }

        return outcomes.map(this::toResponseWithCount);
    }

    @Transactional(readOnly = true)
    public OutcomeResponse getOutcomeById(UUID id, UUID tenantId) {
        Outcome outcome = outcomeRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", id));
        return toResponseWithCount(outcome);
    }

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomesForTeam(UUID teamId, UUID tenantId) {
        return outcomeRepository.findByTenantIdAndTeamId(tenantId, teamId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomesForProject(UUID projectId, UUID tenantId) {
        return outcomeRepository.findByTenantIdAndProgramId(tenantId, projectId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomesForOwner(UUID ownerId, UUID tenantId) {
        return outcomeRepository.findByTenantIdAndOwnerId(tenantId, ownerId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional
    public OutcomeResponse createOutcome(UUID tenantId, CreateOutcomeRequest request, UUID createdById) {
        Outcome outcome = outcomeMapper.toEntity(request, tenantId, createdById);

        if (request.projectId() != null) {
            Program program = programRepository.findByIdAndTenantId(request.projectId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.projectId()));
            outcome.setProgram(program);
        }

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

        eventPublisher.publishOutcomeCreated(outcome, createdById);

        return toResponseWithCount(outcome);
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
        return toResponseWithCount(outcome);
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
        return toResponseWithCount(outcome);
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

        eventPublisher.publishOutcomeValidated(outcome, validatedById, request.finalMetrics());

        return toResponseWithCount(outcome);
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
        outcome.setInvalidatedBy(invalidatedBy);
        outcome.setValidationNotes(request.reason());

        if (request.finalMetrics() != null) {
            try {
                outcome.setCurrentMetrics(objectMapper.writeValueAsString(request.finalMetrics()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid metrics format", e);
            }
        }

        outcome = outcomeRepository.save(outcome);

        eventPublisher.publishOutcomeInvalidated(outcome, invalidatedById);

        return toResponseWithCount(outcome);
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
        return toResponseWithCount(outcome);
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
        return toResponseWithCount(outcome);
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
                .map(this::toResponseWithCount)
                .toList();
    }

    private OutcomeResponse toResponseWithCount(Outcome outcome) {
        int hypothesisCount = (int) hypothesisRepository.countByOutcomeId(outcome.getId());
        return outcomeMapper.toResponse(outcome, hypothesisCount);
    }
}
