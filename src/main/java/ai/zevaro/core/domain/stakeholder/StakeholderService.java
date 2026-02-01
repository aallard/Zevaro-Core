package ai.zevaro.core.domain.stakeholder;

import ai.zevaro.core.domain.decision.Decision;
import ai.zevaro.core.domain.decision.DecisionMapper;
import ai.zevaro.core.domain.decision.DecisionRepository;
import ai.zevaro.core.domain.decision.DecisionStatus;
import ai.zevaro.core.domain.decision.dto.DecisionResponse;
import ai.zevaro.core.domain.decision.dto.DecisionSummary;
import ai.zevaro.core.domain.stakeholder.dto.CreateStakeholderRequest;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderLeaderboard;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderMetrics;
import ai.zevaro.core.domain.stakeholder.dto.StakeholderResponse;
import ai.zevaro.core.domain.stakeholder.dto.UpdateStakeholderRequest;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StakeholderService {

    private final StakeholderRepository stakeholderRepository;
    private final UserRepository userRepository;
    private final DecisionRepository decisionRepository;
    private final StakeholderMapper stakeholderMapper;
    private final DecisionMapper decisionMapper;

    @Transactional(readOnly = true)
    public List<StakeholderResponse> getStakeholders(UUID tenantId, StakeholderType type, Boolean activeOnly) {
        List<Stakeholder> stakeholders;

        if (type != null) {
            stakeholders = stakeholderRepository.findByTenantIdAndType(tenantId, type);
        } else if (Boolean.TRUE.equals(activeOnly)) {
            stakeholders = stakeholderRepository.findByTenantIdAndActiveTrue(tenantId);
        } else {
            stakeholders = stakeholderRepository.findByTenantId(tenantId);
        }

        return stakeholders.stream()
                .map(stakeholderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StakeholderResponse getStakeholderById(UUID id, UUID tenantId) {
        Stakeholder stakeholder = stakeholderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "id", id));
        return stakeholderMapper.toResponse(stakeholder);
    }

    @Transactional(readOnly = true)
    public StakeholderResponse getStakeholderByEmail(String email, UUID tenantId) {
        Stakeholder stakeholder = stakeholderRepository.findByEmailAndTenantId(email, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "email", email));
        return stakeholderMapper.toResponse(stakeholder);
    }

    @Transactional
    public StakeholderResponse createStakeholder(UUID tenantId, CreateStakeholderRequest request, UUID createdById) {
        if (stakeholderRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
            throw new IllegalArgumentException("Stakeholder with email already exists: " + request.email());
        }

        Stakeholder stakeholder = stakeholderMapper.toEntity(request, tenantId, createdById);

        if (request.userId() != null) {
            User user = userRepository.findByIdAndTenantId(request.userId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));
            stakeholder.setUser(user);
        }

        stakeholder = stakeholderRepository.save(stakeholder);
        return stakeholderMapper.toResponse(stakeholder);
    }

    @Transactional
    public StakeholderResponse updateStakeholder(UUID id, UUID tenantId, UpdateStakeholderRequest request) {
        Stakeholder stakeholder = stakeholderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "id", id));

        if (request.email() != null && !request.email().equals(stakeholder.getEmail())) {
            if (stakeholderRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
                throw new IllegalArgumentException("Stakeholder with email already exists: " + request.email());
            }
        }

        stakeholderMapper.updateEntity(stakeholder, request);

        if (request.userId() != null) {
            User user = userRepository.findByIdAndTenantId(request.userId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));
            stakeholder.setUser(user);
        }

        stakeholder = stakeholderRepository.save(stakeholder);
        return stakeholderMapper.toResponse(stakeholder);
    }

    @Transactional
    public void deleteStakeholder(UUID id, UUID tenantId) {
        Stakeholder stakeholder = stakeholderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "id", id));

        if (stakeholder.getDecisionsPending() != null && stakeholder.getDecisionsPending() > 0) {
            throw new IllegalStateException("Cannot delete stakeholder with pending decisions");
        }

        stakeholder.setActive(false);
        stakeholderRepository.save(stakeholder);
    }

    @Transactional(readOnly = true)
    public StakeholderMetrics getStakeholderMetrics(UUID id, UUID tenantId) {
        Stakeholder stakeholder = stakeholderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "id", id));

        List<DecisionSummary> pendingDecisions = new ArrayList<>();
        if (stakeholder.getUser() != null) {
            List<Decision> decisions = decisionRepository.findMyPendingDecisions(stakeholder.getUser().getId());
            pendingDecisions = decisions.stream()
                    .map(decisionMapper::toSummary)
                    .toList();
        }

        return stakeholderMapper.toMetrics(stakeholder, pendingDecisions);
    }

    @Transactional(readOnly = true)
    public StakeholderLeaderboard getLeaderboard(UUID tenantId) {
        List<Stakeholder> fastest = stakeholderRepository.findFastestResponders(tenantId);
        List<StakeholderMetrics> fastestMetrics = fastest.stream()
                .limit(10)
                .map(s -> stakeholderMapper.toMetrics(s, List.of()))
                .toList();

        List<Stakeholder> allActive = stakeholderRepository.findByTenantIdAndActiveTrue(tenantId);

        List<StakeholderMetrics> mostActive = allActive.stream()
                .filter(s -> s.getDecisionsCompleted() != null && s.getDecisionsCompleted() > 0)
                .sorted(Comparator.comparing(Stakeholder::getDecisionsCompleted).reversed())
                .limit(10)
                .map(s -> stakeholderMapper.toMetrics(s, List.of()))
                .toList();

        List<StakeholderMetrics> needingAttention = allActive.stream()
                .filter(s -> (s.getDecisionsPending() != null && s.getDecisionsPending() > 2)
                        || (s.getAvgResponseTimeHours() != null && s.getAvgResponseTimeHours() > 24))
                .sorted(Comparator.comparing((Stakeholder s) -> s.getDecisionsPending() != null ? s.getDecisionsPending() : 0).reversed())
                .limit(10)
                .map(s -> stakeholderMapper.toMetrics(s, List.of()))
                .toList();

        return new StakeholderLeaderboard(fastestMetrics, mostActive, needingAttention);
    }

    @Transactional(readOnly = true)
    public List<StakeholderResponse> getStakeholdersWithPendingDecisions(UUID tenantId) {
        return stakeholderRepository.findWithPendingDecisions(tenantId).stream()
                .map(stakeholderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StakeholderResponse> getSlowResponders(UUID tenantId, double thresholdHours) {
        return stakeholderRepository.findSlowResponders(tenantId, thresholdHours).stream()
                .map(stakeholderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StakeholderResponse> findByExpertise(UUID tenantId, String expertise) {
        return stakeholderRepository.findByExpertiseContaining(tenantId, expertise).stream()
                .map(stakeholderMapper::toResponse)
                .toList();
    }

    @Transactional
    public void onDecisionAssigned(UUID stakeholderId) {
        stakeholderRepository.incrementPendingDecisions(stakeholderId);
    }

    @Transactional
    public void onDecisionCompleted(UUID stakeholderId, Instant decidedAt, double responseTimeHours) {
        stakeholderRepository.recordDecisionCompleted(stakeholderId, decidedAt);
        updateAverageResponseTime(stakeholderId, responseTimeHours);
    }

    @Transactional
    public void onDecisionEscalated(UUID stakeholderId) {
        stakeholderRepository.incrementEscalatedDecisions(stakeholderId);
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getMyPendingResponses(UUID userId, UUID tenantId) {
        // Get decisions assigned to this user that are pending
        List<Decision> decisions = decisionRepository.findByTenantIdAndAssignedToIdAndStatusIn(
                tenantId, userId,
                List.of(DecisionStatus.NEEDS_INPUT, DecisionStatus.UNDER_DISCUSSION));
        return decisions.stream()
                .map(d -> decisionMapper.toResponse(d, 0))
                .toList();
    }

    private void updateAverageResponseTime(UUID stakeholderId, double newResponseTimeHours) {
        Stakeholder stakeholder = stakeholderRepository.findById(stakeholderId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "id", stakeholderId));

        if (stakeholder.getAvgResponseTimeHours() == null) {
            stakeholder.setAvgResponseTimeHours(newResponseTimeHours);
        } else {
            int n = stakeholder.getDecisionsCompleted();
            if (n > 0) {
                double newAvg = (stakeholder.getAvgResponseTimeHours() * (n - 1) + newResponseTimeHours) / n;
                stakeholder.setAvgResponseTimeHours(newAvg);
            }
        }
        stakeholderRepository.save(stakeholder);
    }
}
