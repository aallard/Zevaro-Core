package ai.zevaro.core.domain.program;

import ai.zevaro.core.domain.decision.DecisionRepository;
import ai.zevaro.core.domain.experiment.ExperimentRepository;
import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.outcome.OutcomeRepository;
import ai.zevaro.core.domain.audit.AuditLog;
import ai.zevaro.core.domain.audit.AuditLogRepository;
import ai.zevaro.core.domain.decision.Decision;
import ai.zevaro.core.domain.decision.DecisionStatus;
import ai.zevaro.core.domain.outcome.KeyResult;
import ai.zevaro.core.domain.outcome.Outcome;
import ai.zevaro.core.domain.outcome.OutcomeStatus;
import ai.zevaro.core.domain.program.dto.CreateProgramRequest;
import ai.zevaro.core.domain.program.dto.ProgramDashboardResponse;
import ai.zevaro.core.domain.program.dto.ProgramDashboardResponse.ActivityItem;
import ai.zevaro.core.domain.program.dto.ProgramDashboardResponse.DecisionQueueItem;
import ai.zevaro.core.domain.program.dto.ProgramDashboardResponse.DailyMetric;
import ai.zevaro.core.domain.program.dto.ProgramDashboardResponse.OutcomeProgressItem;
import ai.zevaro.core.domain.program.dto.ProgramResponse;
import ai.zevaro.core.domain.program.dto.ProgramStatsResponse;
import ai.zevaro.core.domain.program.dto.UpdateProgramRequest;
import ai.zevaro.core.domain.user.User;
import ai.zevaro.core.domain.user.UserRepository;
import ai.zevaro.core.event.EventPublisher;
import ai.zevaro.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final OutcomeRepository outcomeRepository;
    private final HypothesisRepository hypothesisRepository;
    private final DecisionRepository decisionRepository;
    private final ExperimentRepository experimentRepository;
    private final UserRepository userRepository;
    private final ProgramMapper programMapper;
    private final EventPublisher eventPublisher;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<ProgramResponse> getPrograms(UUID tenantId, ProgramStatus status) {
        List<Program> programs;

        if (status != null) {
            programs = programRepository.findByTenantIdAndStatus(tenantId, status);
        } else {
            programs = programRepository.findByTenantId(tenantId);
        }

        return programs.stream()
                .map(this::toResponseWithCounts)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProgramResponse> getProgramsPaged(UUID tenantId, ProgramStatus status, Pageable pageable) {
        Page<Program> programs;

        if (status != null) {
            programs = programRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        } else {
            programs = programRepository.findByTenantId(tenantId, pageable);
        }

        return programs.map(this::toResponseWithCounts);
    }

    @Transactional(readOnly = true)
    public ProgramResponse getProgramById(UUID id, UUID tenantId) {
        Program program = programRepository.findByIdAndTenantIdWithDetails(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));
        return toResponseWithCounts(program);
    }

    @Transactional
    public ProgramResponse createProgram(UUID tenantId, CreateProgramRequest request, UUID createdById) {
        Program program = programMapper.toEntity(request, tenantId, createdById);

        // Check slug uniqueness and generate unique slug if needed
        String baseSlug = program.getSlug();
        String finalSlug = baseSlug;
        int counter = 2;

        while (programRepository.findBySlugAndTenantId(finalSlug, tenantId).isPresent()) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        program.setSlug(finalSlug);

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            program.setOwner(owner);
        }

        program = programRepository.save(program);

        // TODO: Implement publishProgramCreated when EventPublisher method is added
        // eventPublisher.publishProgramCreated(program, createdById);

        return toResponseWithCounts(program);
    }

    @Transactional
    public ProgramResponse updateProgram(UUID id, UUID tenantId, UpdateProgramRequest request) {
        Program program = programRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));

        programMapper.updateEntity(program, request);

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            program.setOwner(owner);
        }

        program = programRepository.save(program);

        // TODO: Implement publishProgramUpdated when EventPublisher method is added
        // eventPublisher.publishProgramUpdated(program);

        return toResponseWithCounts(program);
    }

    @Transactional
    public void deleteProgram(UUID id, UUID tenantId) {
        Program program = programRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));

        // Soft delete: archive the program
        program.setStatus(ProgramStatus.ARCHIVED);
        programRepository.save(program);
    }

    @Transactional(readOnly = true)
    public ProgramStatsResponse getProgramStats(UUID id, UUID tenantId) {
        Program program = programRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", id));

        int pendingDecisionCount = (int) decisionRepository.countByTenantIdAndProjectIdAndStatus(
                tenantId, id, ai.zevaro.core.domain.decision.DecisionStatus.NEEDS_INPUT);
        int activeOutcomeCount = (int) outcomeRepository.findByTenantIdAndProgramIdAndStatus(
                tenantId, id, ai.zevaro.core.domain.outcome.OutcomeStatus.IN_PROGRESS).size();
        int totalHypothesisCount = (int) hypothesisRepository.countByTenantIdAndProjectId(tenantId, id);

        return new ProgramStatsResponse(
                pendingDecisionCount,
                activeOutcomeCount,
                0,
                totalHypothesisCount
        );
    }

    @Transactional(readOnly = true)
    public ProgramDashboardResponse getProgramDashboard(UUID programId, UUID tenantId) {
        Program program = programRepository.findByIdAndTenantId(programId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Program", "id", programId));

        // Count pending decisions
        int pendingDecisionCount = (int) decisionRepository.countByTenantIdAndProjectIdAndStatus(
                tenantId, programId, DecisionStatus.NEEDS_INPUT);

        // Count SLA breached decisions
        List<Decision> slaBreachedDecisions = decisionRepository.findSlaBreachedForProject(tenantId, programId);
        int slaBreachedDecisionCount = slaBreachedDecisions.size();

        // Count active outcomes (IN_PROGRESS)
        int activeOutcomeCount = (int) outcomeRepository.countByTenantIdAndProjectIdAndStatus(
                tenantId, programId, OutcomeStatus.IN_PROGRESS);

        // Calculate outcome validation percentage
        long validatedCount = outcomeRepository.countValidatedForProject(tenantId, programId);
        long totalNonDraftCount = outcomeRepository.countNonDraftForProject(tenantId, programId);
        double outcomeValidationPercentage = totalNonDraftCount > 0 ? (validatedCount * 100.0 / totalNonDraftCount) : 0;

        // Count running experiments
        long runningExperimentCount = experimentRepository.countRunningForProject(tenantId, programId);

        // Calculate average decision time
        Double avgDecisionTimeHours = decisionRepository.getAverageDecisionTimeHours(tenantId, Instant.now().minus(Duration.ofDays(30)));
        if (avgDecisionTimeHours == null) {
            avgDecisionTimeHours = 0.0;
        }

        // Calculate trend (simplified: compare to 30-60 days ago)
        Double previousAvgTime = decisionRepository.getAverageDecisionTimeHours(
                tenantId, Instant.now().minus(Duration.ofDays(60)));
        if (previousAvgTime == null) {
            previousAvgTime = avgDecisionTimeHours;
        }
        double avgDecisionTimeTrend = avgDecisionTimeHours - previousAvgTime;

        // Get urgent decisions (top 5)
        List<Decision> urgentDecisions = decisionRepository.findUrgentDecisionsForProject(tenantId, programId);
        List<DecisionQueueItem> decisionQueueItems = urgentDecisions.stream()
                .map(d -> new DecisionQueueItem(
                    d.getId(),
                    d.getTitle(),
                    d.getPriority() != null ? d.getPriority().toString() : "NORMAL",
                    d.getAssignedTo() != null ? d.getAssignedTo().getFullName() : "Unassigned",
                    d.getAssignedTo() != null ? d.getAssignedTo().getAvatarUrl() : null,
                    d.getWaitTimeHours() * 60, // convert hours to minutes
                    slaBreachedDecisions.contains(d)
                ))
                .toList();

        // Build decision velocity (last 30 days)
        Instant since = Instant.now().minus(Duration.ofDays(30));
        List<Object[]> dailyMetricsData = decisionRepository.findDailyMetricsForProject(tenantId, programId, since);
        Map<LocalDate, DailyMetric> metricsMap = new HashMap<>();
        for (Object[] row : dailyMetricsData) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            int count = ((Number) row[1]).intValue();
            double avgHours = ((Number) row[2]).doubleValue();
            metricsMap.put(date, new DailyMetric(date, count, avgHours));
        }
        List<DailyMetric> decisionVelocity = new ArrayList<>(metricsMap.values());

        // Get outcome progress
        List<Outcome> activeOutcomes = outcomeRepository.findByTenantIdAndProgramIdAndStatus(
                tenantId, programId, OutcomeStatus.IN_PROGRESS);
        List<OutcomeProgressItem> outcomeProgress = activeOutcomes.stream()
                .map(o -> {
                    double progressPercent = 0;
                    if (!o.getKeyResults().isEmpty()) {
                        double totalProgress = o.getKeyResults().stream()
                                .mapToDouble(kr -> kr.getProgressPercent().doubleValue())
                                .sum();
                        progressPercent = totalProgress / o.getKeyResults().size();
                    }
                    String color = progressPercent < 30 ? "red" : progressPercent < 70 ? "yellow" : "green";
                    return new OutcomeProgressItem(
                        o.getId(),
                        o.getTitle(),
                        o.getStatus().toString(),
                        progressPercent,
                        color
                    );
                })
                .toList();

        // Get recent activity
        List<AuditLog> recentActivityLogs = auditLogRepository.findRecentActivityForTenant(tenantId, 20);
        List<ActivityItem> recentActivity = recentActivityLogs.stream()
                .map(log -> new ActivityItem(
                    log.getActorName() != null ? log.getActorName() : "Unknown",
                    null, // TODO: Could add actor avatar URL if available
                    log.getAction() != null ? log.getAction().toString() : "UNKNOWN",
                    log.getEntityType(),
                    log.getEntityTitle(),
                    log.getTimestamp()
                ))
                .toList();

        return new ProgramDashboardResponse(
            pendingDecisionCount,
            slaBreachedDecisionCount,
            activeOutcomeCount,
            outcomeValidationPercentage,
            (int) runningExperimentCount,
            avgDecisionTimeHours,
            avgDecisionTimeTrend,
            decisionQueueItems,
            decisionVelocity,
            outcomeProgress,
            recentActivity
        );
    }

    private ProgramResponse toResponseWithCounts(Program program) {
        int decisionCount = (int) decisionRepository.countByTenantIdAndProjectId(program.getTenantId(), program.getId());
        int outcomeCount = (int) outcomeRepository.countByTenantIdAndProjectId(program.getTenantId(), program.getId());
        int hypothesisCount = (int) hypothesisRepository.countByTenantIdAndProjectId(program.getTenantId(), program.getId());
        int teamMemberCount = 0;

        return programMapper.toResponse(program, decisionCount, outcomeCount, hypothesisCount, teamMemberCount);
    }
}
