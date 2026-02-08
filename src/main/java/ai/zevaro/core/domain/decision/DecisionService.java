package ai.zevaro.core.domain.decision;

import ai.zevaro.core.config.AppConstants;
import ai.zevaro.core.domain.decision.dto.BlockedItem;
import ai.zevaro.core.domain.decision.dto.CastVoteRequest;
import ai.zevaro.core.domain.decision.dto.CommentResponse;
import ai.zevaro.core.domain.decision.dto.CreateCommentRequest;
import ai.zevaro.core.domain.decision.dto.CreateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.DecisionQueueResponse;
import ai.zevaro.core.domain.decision.dto.DecisionResponse;
import ai.zevaro.core.domain.decision.dto.EscalateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.ResolveDecisionRequest;
import ai.zevaro.core.domain.decision.dto.UpdateCommentRequest;
import ai.zevaro.core.domain.decision.dto.UpdateDecisionRequest;
import ai.zevaro.core.domain.decision.dto.VoteResponse;
import ai.zevaro.core.domain.decision.dto.VoteSummary;
import ai.zevaro.core.domain.hypothesis.Hypothesis;
import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.hypothesis.HypothesisStatus;
import ai.zevaro.core.domain.outcome.Outcome;
import ai.zevaro.core.domain.outcome.OutcomeRepository;
import ai.zevaro.core.domain.program.Program;
import ai.zevaro.core.domain.program.ProgramRepository;
import ai.zevaro.core.domain.queue.DecisionQueue;
import ai.zevaro.core.domain.queue.DecisionQueueRepository;
import ai.zevaro.core.domain.stakeholder.Stakeholder;
import ai.zevaro.core.domain.stakeholder.StakeholderRepository;
import ai.zevaro.core.domain.stakeholder.StakeholderService;
import ai.zevaro.core.domain.team.Team;
import ai.zevaro.core.domain.team.TeamRepository;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.domain.workstream.Workstream;
import ai.zevaro.core.domain.workstream.WorkstreamRepository;
import ai.zevaro.core.event.EventPublisher;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final DecisionCommentRepository commentRepository;
    private final DecisionVoteRepository voteRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ProgramRepository programRepository;
    private final OutcomeRepository outcomeRepository;
    private final HypothesisRepository hypothesisRepository;
    private final DecisionQueueRepository queueRepository;
    private final StakeholderRepository stakeholderRepository;
    private final StakeholderService stakeholderService;
    private final WorkstreamRepository workstreamRepository;
    private final DecisionMapper decisionMapper;
    private final EventPublisher eventPublisher;

    private static final Set<DecisionStatus> OPEN_STATUSES = Set.of(
            DecisionStatus.NEEDS_INPUT,
            DecisionStatus.UNDER_DISCUSSION
    );

    private static final Set<DecisionStatus> TERMINAL_STATUSES = Set.of(
            DecisionStatus.IMPLEMENTED,
            DecisionStatus.CANCELLED
    );

    @Transactional(readOnly = true)
    public List<DecisionResponse> getDecisions(UUID tenantId, DecisionStatus status, DecisionPriority priority,
                                                DecisionType type, UUID teamId, UUID projectId) {
        List<Decision> decisions;

        if (projectId != null) {
            decisions = decisionRepository.findByTenantIdAndProgramId(tenantId, projectId);
        } else if (status != null) {
            decisions = decisionRepository.findByTenantIdAndStatus(tenantId, status);
        } else if (priority != null) {
            decisions = decisionRepository.findByTenantIdAndPriority(tenantId, priority);
        } else if (type != null) {
            decisions = decisionRepository.findByTenantIdAndDecisionType(tenantId, type);
        } else if (teamId != null) {
            decisions = decisionRepository.findByTeamId(teamId);
        } else {
            decisions = decisionRepository.findByTenantId(tenantId);
        }

        return decisions.stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DecisionResponse> getDecisionsPaged(UUID tenantId, DecisionStatus status, DecisionPriority priority,
                                                     DecisionType type, UUID teamId, UUID projectId, Pageable pageable) {
        Page<Decision> decisions;

        if (projectId != null) {
            decisions = decisionRepository.findByTenantIdAndProgramId(tenantId, projectId, pageable);
        } else if (status != null) {
            decisions = decisionRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        } else if (priority != null) {
            decisions = decisionRepository.findByTenantIdAndPriority(tenantId, priority, pageable);
        } else if (type != null) {
            decisions = decisionRepository.findByTenantIdAndDecisionType(tenantId, type, pageable);
        } else if (teamId != null) {
            decisions = decisionRepository.findByTeamId(teamId, pageable);
        } else {
            decisions = decisionRepository.findByTenantId(tenantId, pageable);
        }

        return decisions.map(this::toResponseWithCount);
    }

    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(UUID id, UUID tenantId) {
        return getDecisionById(id, tenantId, false, false);
    }

    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(UUID id, UUID tenantId, boolean includeVotes, boolean includeComments) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        int commentCount = commentRepository.countByDecisionId(decision.getId());
        List<DecisionVote> voteEntities = voteRepository.findByDecisionId(decision.getId());
        int voteCount = voteEntities.size();

        List<VoteResponse> votes = null;
        List<CommentResponse> comments = null;

        if (includeVotes) {
            votes = voteEntities.stream()
                    .map(decisionMapper::toVoteResponse)
                    .toList();
        }

        if (includeComments) {
            comments = commentRepository.findByDecisionIdOrderByCreatedAtAsc(decision.getId()).stream()
                    .map(decisionMapper::toCommentResponse)
                    .toList();
        }

        return decisionMapper.toResponse(decision, commentCount, voteCount, votes, comments);
    }

    @Transactional(readOnly = true)
    public DecisionQueueResponse getDecisionQueue(UUID tenantId) {
        List<DecisionStatus> openStatuses = List.of(DecisionStatus.NEEDS_INPUT, DecisionStatus.UNDER_DISCUSSION);
        List<Decision> queueDecisions = decisionRepository.findDecisionQueue(tenantId, openStatuses);

        List<DecisionResponse> needsInput = queueDecisions.stream()
                .filter(d -> d.getStatus() == DecisionStatus.NEEDS_INPUT)
                .map(this::toResponseWithCount)
                .toList();

        List<DecisionResponse> underDiscussion = queueDecisions.stream()
                .filter(d -> d.getStatus() == DecisionStatus.UNDER_DISCUSSION)
                .map(this::toResponseWithCount)
                .toList();

        List<Decision> recentlyDecided = decisionRepository.findByTenantIdAndStatus(tenantId, DecisionStatus.DECIDED);
        List<DecisionResponse> decided = recentlyDecided.stream()
                .map(this::toResponseWithCount)
                .toList();

        long totalPending = decisionRepository.countPendingDecisions(tenantId);
        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        Double avgTime = decisionRepository.getAverageDecisionTimeHours(tenantId, thirtyDaysAgo);

        return new DecisionQueueResponse(needsInput, underDiscussion, decided, totalPending, avgTime);
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getMyPendingDecisions(UUID userId) {
        return decisionRepository.findMyPendingDecisions(userId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getOverdueDecisions(UUID tenantId) {
        return decisionRepository.findOverdueDecisions(tenantId, Instant.now()).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getPendingDecisions(UUID tenantId, UUID teamId) {
        List<Decision> decisions;
        if (teamId != null) {
            decisions = decisionRepository.findByTenantIdAndStatusInAndTeamId(
                    tenantId, List.of(DecisionStatus.NEEDS_INPUT, DecisionStatus.UNDER_DISCUSSION), teamId);
        } else {
            decisions = decisionRepository.findByTenantIdAndStatusIn(
                    tenantId, List.of(DecisionStatus.NEEDS_INPUT, DecisionStatus.UNDER_DISCUSSION));
        }
        return decisions.stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getBlockingDecisions(UUID tenantId) {
        return decisionRepository.findByTenantIdAndPriorityAndStatusIn(
                tenantId, DecisionPriority.BLOCKING,
                List.of(DecisionStatus.NEEDS_INPUT, DecisionStatus.UNDER_DISCUSSION)).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getDecisionsForProject(UUID projectId, UUID tenantId) {
        return decisionRepository.findByTenantIdAndProgramId(tenantId, projectId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getDecisionsForOutcome(UUID outcomeId, UUID tenantId) {
        return decisionRepository.findByOutcomeId(outcomeId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> getDecisionsForHypothesis(UUID hypothesisId, UUID tenantId) {
        return decisionRepository.findByHypothesisId(hypothesisId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> listByParent(DecisionParentType parentType, UUID parentId, UUID tenantId) {
        return decisionRepository.findByTenantIdAndParentTypeAndParentId(tenantId, parentType, parentId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DecisionResponse> listByWorkstream(UUID workstreamId, UUID tenantId) {
        workstreamRepository.findByIdAndTenantId(workstreamId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", workstreamId));
        return decisionRepository.findByTenantIdAndWorkstreamId(tenantId, workstreamId).stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    @Transactional
    public DecisionResponse createDecision(UUID tenantId, CreateDecisionRequest request, UUID createdById) {
        Decision decision = decisionMapper.toEntity(request, tenantId, createdById);

        int slaHours = request.slaHours() != null ? request.slaHours() : getSlaHours(request.priority());
        decision.setSlaHours(slaHours);
        decision.setDueAt(Instant.now().plus(Duration.ofHours(slaHours)));

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            decision.setOwner(owner);
        }

        if (request.assignedToId() != null) {
            User assignedTo = userRepository.findByIdAndTenantId(request.assignedToId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));
            decision.setAssignedTo(assignedTo);
        }

        if (request.outcomeId() != null) {
            Outcome outcome = outcomeRepository.findByIdAndTenantId(request.outcomeId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", request.outcomeId()));
            decision.setOutcome(outcome);
        }

        if (request.hypothesisId() != null) {
            Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(request.hypothesisId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", request.hypothesisId()));
            decision.setHypothesis(hypothesis);
        }

        if (request.teamId() != null) {
            Team team = teamRepository.findByIdAndTenantId(request.teamId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.teamId()));
            decision.setTeam(team);
        }

        if (request.projectId() != null) {
            Program program = programRepository.findByIdAndTenantId(request.projectId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Program", "id", request.projectId()));
            decision.setProgram(program);
        }

        if (request.queueId() != null) {
            DecisionQueue queue = queueRepository.findByIdAndTenantId(request.queueId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("DecisionQueue", "id", request.queueId()));
            decision.setQueue(queue);
        }

        if (request.stakeholderId() != null) {
            Stakeholder stakeholder = stakeholderRepository.findByIdAndTenantId(request.stakeholderId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "id", request.stakeholderId()));
            decision.setStakeholder(stakeholder);
        }

        // Polymorphic parent support
        if (request.parentType() != null && request.parentId() != null) {
            decision.setParentType(request.parentType());
            decision.setParentId(request.parentId());
        } else if (request.hypothesisId() != null) {
            // Auto-populate from existing FK for backward compat
            decision.setParentType(DecisionParentType.HYPOTHESIS);
            decision.setParentId(request.hypothesisId());
        }

        if (request.workstreamId() != null) {
            Workstream workstream = workstreamRepository.findByIdAndTenantId(request.workstreamId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workstream", "id", request.workstreamId()));
            decision.setWorkstreamId(workstream.getId());
        }

        decision = decisionRepository.save(decision);

        if (decision.getAssignedTo() != null) {
            stakeholderRepository.findByUserIdAndTenantId(decision.getAssignedTo().getId(), tenantId)
                    .ifPresent(stakeholder -> stakeholderService.onDecisionAssigned(stakeholder.getId()));
        }

        eventPublisher.publishDecisionCreated(decision, createdById);

        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse updateDecision(UUID id, UUID tenantId, UpdateDecisionRequest request) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        decisionMapper.updateEntity(decision, request);

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            decision.setOwner(owner);
        }

        if (request.assignedToId() != null) {
            User assignedTo = userRepository.findByIdAndTenantId(request.assignedToId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));
            decision.setAssignedTo(assignedTo);
        }

        if (request.outcomeId() != null) {
            Outcome outcome = outcomeRepository.findByIdAndTenantId(request.outcomeId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Outcome", "id", request.outcomeId()));
            decision.setOutcome(outcome);
        }

        if (request.hypothesisId() != null) {
            Hypothesis hypothesis = hypothesisRepository.findByIdAndTenantId(request.hypothesisId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hypothesis", "id", request.hypothesisId()));
            decision.setHypothesis(hypothesis);
        }

        if (request.teamId() != null) {
            Team team = teamRepository.findByIdAndTenantId(request.teamId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team", "id", request.teamId()));
            decision.setTeam(team);
        }

        if (request.queueId() != null) {
            DecisionQueue queue = queueRepository.findByIdAndTenantId(request.queueId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("DecisionQueue", "id", request.queueId()));
            decision.setQueue(queue);
        }

        if (request.stakeholderId() != null) {
            Stakeholder stakeholder = stakeholderRepository.findByIdAndTenantId(request.stakeholderId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "id", request.stakeholderId()));
            decision.setStakeholder(stakeholder);
        }

        if (request.slaHours() != null) {
            decision.setDueAt(decision.getCreatedAt().plus(Duration.ofHours(request.slaHours())));
        }

        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public void deleteDecision(UUID id, UUID tenantId) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));
        decisionRepository.delete(decision);
    }

    @Transactional
    public DecisionResponse startDiscussion(UUID id, UUID tenantId) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        if (decision.getStatus() != DecisionStatus.NEEDS_INPUT) {
            throw new IllegalStateException("Can only start discussion from NEEDS_INPUT status");
        }

        decision.setStatus(DecisionStatus.UNDER_DISCUSSION);
        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse resolve(UUID id, UUID tenantId, ResolveDecisionRequest request, UUID decidedById) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        if (!OPEN_STATUSES.contains(decision.getStatus())) {
            throw new IllegalStateException("Can only resolve decisions in open status");
        }

        User decidedBy = userRepository.findByIdAndTenantId(decidedById, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", decidedById));

        decision.setStatus(DecisionStatus.DECIDED);
        decision.setDecidedAt(Instant.now());
        decision.setDecidedBy(decidedBy);
        decision.setDecisionRationale(request.rationale());

        if (request.selectedOption() != null) {
            decision.setSelectedOption(decisionMapper.selectedOptionToJson(request.selectedOption()));
        }

        Decision savedDecision = decisionRepository.save(decision);

        List<UUID> unblockedHypothesisIds = unblockHypotheses(savedDecision, tenantId);

        if (savedDecision.getAssignedTo() != null) {
            Instant createdAt = savedDecision.getCreatedAt();
            UUID assignedToUserId = savedDecision.getAssignedTo().getId();
            stakeholderRepository.findByUserIdAndTenantId(assignedToUserId, tenantId)
                    .ifPresent(stakeholder -> {
                        double responseTimeHours = Duration.between(createdAt, Instant.now()).toHours();
                        stakeholderService.onDecisionCompleted(stakeholder.getId(), Instant.now(), responseTimeHours);
                    });
        }

        eventPublisher.publishDecisionResolved(savedDecision, decidedById, unblockedHypothesisIds);

        return toResponseWithCount(savedDecision);
    }

    @Transactional
    public DecisionResponse implement(UUID id, UUID tenantId) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        if (decision.getStatus() != DecisionStatus.DECIDED) {
            throw new IllegalStateException("Can only implement decisions in DECIDED status");
        }

        decision.setStatus(DecisionStatus.IMPLEMENTED);
        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse defer(UUID id, UUID tenantId, String reason) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        if (!OPEN_STATUSES.contains(decision.getStatus())) {
            throw new IllegalStateException("Can only defer decisions in open status");
        }

        decision.setStatus(DecisionStatus.DEFERRED);
        decision.setDecisionRationale(reason);
        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse cancel(UUID id, UUID tenantId, String reason) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        if (TERMINAL_STATUSES.contains(decision.getStatus())) {
            throw new IllegalStateException("Cannot cancel a decision in terminal status");
        }

        decision.setStatus(DecisionStatus.CANCELLED);
        decision.setDecisionRationale(reason);
        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse reopen(UUID id, UUID tenantId) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        if (decision.getStatus() != DecisionStatus.DEFERRED && decision.getStatus() != DecisionStatus.CANCELLED) {
            throw new IllegalStateException("Can only reopen DEFERRED or CANCELLED decisions");
        }

        decision.setStatus(DecisionStatus.NEEDS_INPUT);
        decision.setDecisionRationale(null);
        decision.setDueAt(Instant.now().plus(Duration.ofHours(decision.getSlaHours())));
        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse escalate(UUID id, UUID tenantId, EscalateDecisionRequest request, UUID escalatedById) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        if (!OPEN_STATUSES.contains(decision.getStatus())) {
            throw new IllegalStateException("Can only escalate decisions in open status");
        }

        User escalatedTo = userRepository.findByIdAndTenantId(request.escalateToId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.escalateToId()));

        User previousAssignee = decision.getAssignedTo();

        decision.setEscalationLevel(decision.getEscalationLevel() + 1);
        decision.setEscalatedAt(Instant.now());
        decision.setEscalatedTo(escalatedTo);
        decision.setAssignedTo(escalatedTo);

        decision = decisionRepository.save(decision);

        if (previousAssignee != null) {
            stakeholderRepository.findByUserIdAndTenantId(previousAssignee.getId(), tenantId)
                    .ifPresent(stakeholder -> stakeholderService.onDecisionEscalated(stakeholder.getId()));
        }

        eventPublisher.publishDecisionEscalated(
                decision,
                escalatedById,
                previousAssignee != null ? previousAssignee.getId() : null,
                request.reason()
        );

        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse assign(UUID id, UUID tenantId, UUID assignedToId) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        User assignedTo = userRepository.findByIdAndTenantId(assignedToId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedToId));

        decision.setAssignedTo(assignedTo);
        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse reassign(UUID id, UUID tenantId, UUID newAssigneeId, String reason) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        User previousAssignee = decision.getAssignedTo();
        User newAssignee = userRepository.findByIdAndTenantId(newAssigneeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", newAssigneeId));

        decision.setAssignedTo(newAssignee);

        if (reason != null && !reason.isBlank()) {
            String previousName = previousAssignee != null
                    ? previousAssignee.getFirstName() + " " + previousAssignee.getLastName()
                    : "unassigned";
            String newName = newAssignee.getFirstName() + " " + newAssignee.getLastName();
            String reassignNote = String.format("[Reassigned from %s to %s] %s", previousName, newName, reason);

            String existingContext = decision.getContext();
            if (existingContext != null && !existingContext.isBlank()) {
                decision.setContext(existingContext + "\n\n" + reassignNote);
            } else {
                decision.setContext(reassignNote);
            }
        }

        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional
    public DecisionResponse addBlockedItem(UUID id, UUID tenantId, BlockedItem item) {
        Decision decision = decisionRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        List<BlockedItem> items = decisionMapper.toResponse(decision, 0).blockedItems();
        if (items == null) {
            items = new java.util.ArrayList<>();
        } else {
            items = new java.util.ArrayList<>(items);
        }
        items.add(item);

        decision.setBlockedItems(decisionMapper.blockedItemsToJson(items));
        decision = decisionRepository.save(decision);
        return toResponseWithCount(decision);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID decisionId, UUID tenantId) {
        decisionRepository.findByIdAndTenantId(decisionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        return commentRepository.findByDecisionIdOrderByCreatedAtAsc(decisionId).stream()
                .map(decisionMapper::toCommentResponse)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(UUID decisionId, UUID tenantId, CreateCommentRequest request, UUID authorId) {
        Decision decision = decisionRepository.findByIdAndTenantId(decisionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        User author = userRepository.findByIdAndTenantId(authorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        DecisionComment comment = new DecisionComment();
        comment.setDecision(decision);
        comment.setAuthor(author);
        comment.setContent(request.content());
        comment.setOptionId(request.optionId());

        if (request.parentId() != null) {
            DecisionComment parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.parentId()));
            comment.setParent(parent);
        }

        comment = commentRepository.save(comment);
        return decisionMapper.toCommentResponse(comment);
    }

    @Transactional
    public CommentResponse updateComment(UUID commentId, UUID tenantId, UpdateCommentRequest request, UUID userId) {
        DecisionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the author can edit their comment");
        }

        comment.setContent(request.content());
        comment.setEdited(true);
        comment = commentRepository.save(comment);
        return decisionMapper.toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID tenantId, UUID userId) {
        DecisionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the author can delete their comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Map<DecisionStatus, Long> getStatusCounts(UUID tenantId) {
        List<Object[]> results = decisionRepository.countByStatusForTenant(tenantId);
        Map<DecisionStatus, Long> counts = new EnumMap<>(DecisionStatus.class);

        for (DecisionStatus status : DecisionStatus.values()) {
            counts.put(status, 0L);
        }

        for (Object[] result : results) {
            DecisionStatus status = (DecisionStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status, count);
        }

        return counts;
    }

    // Vote methods

    @Transactional(readOnly = true)
    public List<VoteResponse> getVotes(UUID decisionId, UUID tenantId) {
        decisionRepository.findByIdAndTenantId(decisionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        return voteRepository.findByDecisionId(decisionId).stream()
                .map(decisionMapper::toVoteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VoteSummary getVoteSummary(UUID decisionId, UUID tenantId) {
        decisionRepository.findByIdAndTenantId(decisionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        List<DecisionVote> votes = voteRepository.findByDecisionId(decisionId);
        List<VoteResponse> voteResponses = votes.stream()
                .map(decisionMapper::toVoteResponse)
                .toList();

        Map<VoteType, Long> countByType = new EnumMap<>(VoteType.class);
        for (VoteType voteType : VoteType.values()) {
            countByType.put(voteType, 0L);
        }
        for (DecisionVote vote : votes) {
            countByType.merge(vote.getVote(), 1L, Long::sum);
        }

        return new VoteSummary(votes.size(), countByType, voteResponses);
    }

    @Transactional
    public VoteResponse castVote(UUID decisionId, UUID tenantId, CastVoteRequest request, UUID userId) {
        Decision decision = decisionRepository.findByIdAndTenantId(decisionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        User user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        DecisionVote vote = voteRepository.findByDecisionIdAndUserId(decisionId, userId)
                .orElse(null);

        if (vote == null) {
            vote = DecisionVote.builder()
                    .decision(decision)
                    .user(user)
                    .vote(request.vote())
                    .comment(request.comment())
                    .build();
        } else {
            vote.setVote(request.vote());
            vote.setComment(request.comment());
        }

        vote = voteRepository.save(vote);
        return decisionMapper.toVoteResponse(vote);
    }

    @Transactional
    public void removeVote(UUID decisionId, UUID tenantId, UUID userId) {
        decisionRepository.findByIdAndTenantId(decisionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));

        DecisionVote vote = voteRepository.findByDecisionIdAndUserId(decisionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vote", "decisionId and userId", decisionId + "/" + userId));

        voteRepository.delete(vote);
    }

    @Transactional(readOnly = true)
    public Double getAverageDecisionTime(UUID tenantId, int days) {
        Instant since = Instant.now().minus(Duration.ofDays(days));
        return decisionRepository.getAverageDecisionTimeHours(tenantId, since);
    }

    private int getSlaHours(DecisionPriority priority) {
        return switch (priority) {
            case BLOCKING -> AppConstants.SLA_BLOCKING;
            case HIGH -> AppConstants.SLA_HIGH;
            case NORMAL -> AppConstants.SLA_NORMAL;
            case LOW -> AppConstants.SLA_LOW;
        };
    }

    private DecisionResponse toResponseWithCount(Decision decision) {
        int commentCount = commentRepository.countByDecisionId(decision.getId());
        int voteCount = (int) voteRepository.findByDecisionId(decision.getId()).size();
        return decisionMapper.toResponse(decision, commentCount, voteCount);
    }

    private List<UUID> unblockHypotheses(Decision decision, UUID tenantId) {
        List<UUID> unblockedIds = new java.util.ArrayList<>();
        DecisionResponse response = decisionMapper.toResponse(decision, 0);
        if (response.blockedItems() == null) {
            return unblockedIds;
        }

        for (BlockedItem item : response.blockedItems()) {
            if ("hypothesis".equals(item.type())) {
                hypothesisRepository.findByIdAndTenantId(item.id(), tenantId)
                        .ifPresent(hypothesis -> {
                            if (hypothesis.getStatus() == HypothesisStatus.BLOCKED) {
                                hypothesis.setStatus(HypothesisStatus.READY);
                                hypothesis.setBlockedReason(null);
                                hypothesisRepository.save(hypothesis);
                                unblockedIds.add(hypothesis.getId());
                            }
                        });
            }
        }
        return unblockedIds;
    }
}
