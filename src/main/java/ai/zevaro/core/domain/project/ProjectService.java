package ai.zevaro.core.domain.project;

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
import ai.zevaro.core.domain.project.dto.CreateProjectRequest;
import ai.zevaro.core.domain.project.dto.ProjectDashboardResponse;
import ai.zevaro.core.domain.project.dto.ProjectDashboardResponse.ActivityItem;
import ai.zevaro.core.domain.project.dto.ProjectDashboardResponse.DecisionQueueItem;
import ai.zevaro.core.domain.project.dto.ProjectDashboardResponse.DailyMetric;
import ai.zevaro.core.domain.project.dto.ProjectDashboardResponse.OutcomeProgressItem;
import ai.zevaro.core.domain.project.dto.ProjectResponse;
import ai.zevaro.core.domain.project.dto.ProjectStatsResponse;
import ai.zevaro.core.domain.project.dto.UpdateProjectRequest;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OutcomeRepository outcomeRepository;
    private final HypothesisRepository hypothesisRepository;
    private final DecisionRepository decisionRepository;
    private final ExperimentRepository experimentRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final EventPublisher eventPublisher;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(UUID tenantId, ProjectStatus status) {
        List<Project> projects;

        if (status != null) {
            projects = projectRepository.findByTenantIdAndStatus(tenantId, status);
        } else {
            projects = projectRepository.findByTenantId(tenantId);
        }

        return projects.stream()
                .map(this::toResponseWithCounts)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjectsPaged(UUID tenantId, ProjectStatus status, Pageable pageable) {
        Page<Project> projects;

        if (status != null) {
            projects = projectRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        } else {
            projects = projectRepository.findByTenantId(tenantId, pageable);
        }

        return projects.map(this::toResponseWithCounts);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id, UUID tenantId) {
        Project project = projectRepository.findByIdAndTenantIdWithDetails(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return toResponseWithCounts(project);
    }

    @Transactional
    public ProjectResponse createProject(UUID tenantId, CreateProjectRequest request, UUID createdById) {
        Project project = projectMapper.toEntity(request, tenantId, createdById);

        // Check slug uniqueness and generate unique slug if needed
        String baseSlug = project.getSlug();
        String finalSlug = baseSlug;
        int counter = 2;

        while (projectRepository.findBySlugAndTenantId(finalSlug, tenantId).isPresent()) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        project.setSlug(finalSlug);

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            project.setOwner(owner);
        }

        project = projectRepository.save(project);

        // TODO: Implement publishProjectCreated when EventPublisher method is added
        // eventPublisher.publishProjectCreated(project, createdById);

        return toResponseWithCounts(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, UUID tenantId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        projectMapper.updateEntity(project, request);

        if (request.ownerId() != null) {
            User owner = userRepository.findByIdAndTenantId(request.ownerId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.ownerId()));
            project.setOwner(owner);
        }

        project = projectRepository.save(project);

        // TODO: Implement publishProjectUpdated when EventPublisher method is added
        // eventPublisher.publishProjectUpdated(project);

        return toResponseWithCounts(project);
    }

    @Transactional
    public void deleteProject(UUID id, UUID tenantId) {
        Project project = projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Soft delete: archive the project
        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public ProjectStatsResponse getProjectStats(UUID id, UUID tenantId) {
        Project project = projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        int pendingDecisionCount = (int) decisionRepository.countByTenantIdAndProjectIdAndStatus(
                tenantId, id, ai.zevaro.core.domain.decision.DecisionStatus.NEEDS_INPUT);
        int activeOutcomeCount = (int) outcomeRepository.findByTenantIdAndProjectIdAndStatus(
                tenantId, id, ai.zevaro.core.domain.outcome.OutcomeStatus.IN_PROGRESS).size();
        int totalHypothesisCount = (int) hypothesisRepository.countByTenantIdAndProjectId(tenantId, id);

        return new ProjectStatsResponse(
                pendingDecisionCount,
                activeOutcomeCount,
                0,
                totalHypothesisCount
        );
    }

    @Transactional(readOnly = true)
    public ProjectDashboardResponse getProjectDashboard(UUID projectId, UUID tenantId) {
        Project project = projectRepository.findByIdAndTenantId(projectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Count pending decisions
        int pendingDecisionCount = (int) decisionRepository.countByTenantIdAndProjectIdAndStatus(
                tenantId, projectId, DecisionStatus.NEEDS_INPUT);

        // Count SLA breached decisions
        List<Decision> slaBreachedDecisions = decisionRepository.findSlaBreachedForProject(tenantId, projectId);
        int slaBreachedDecisionCount = slaBreachedDecisions.size();

        // Count active outcomes (IN_PROGRESS)
        int activeOutcomeCount = (int) outcomeRepository.countByTenantIdAndProjectIdAndStatus(
                tenantId, projectId, OutcomeStatus.IN_PROGRESS);

        // Calculate outcome validation percentage
        long validatedCount = outcomeRepository.countValidatedForProject(tenantId, projectId);
        long totalNonDraftCount = outcomeRepository.countNonDraftForProject(tenantId, projectId);
        double outcomeValidationPercentage = totalNonDraftCount > 0 ? (validatedCount * 100.0 / totalNonDraftCount) : 0;

        // Count running experiments
        long runningExperimentCount = experimentRepository.countRunningForProject(tenantId, projectId);

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
        List<Decision> urgentDecisions = decisionRepository.findUrgentDecisionsForProject(tenantId, projectId);
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
        List<Object[]> dailyMetricsData = decisionRepository.findDailyMetricsForProject(tenantId, projectId, since);
        Map<LocalDate, DailyMetric> metricsMap = new HashMap<>();
        for (Object[] row : dailyMetricsData) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            int count = ((Number) row[1]).intValue();
            double avgHours = ((Number) row[2]).doubleValue();
            metricsMap.put(date, new DailyMetric(date, count, avgHours));
        }
        List<DailyMetric> decisionVelocity = new ArrayList<>(metricsMap.values());

        // Get outcome progress
        List<Outcome> activeOutcomes = outcomeRepository.findByTenantIdAndProjectIdAndStatus(
                tenantId, projectId, OutcomeStatus.IN_PROGRESS);
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

        return new ProjectDashboardResponse(
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

    private ProjectResponse toResponseWithCounts(Project project) {
        int decisionCount = (int) decisionRepository.countByTenantIdAndProjectId(project.getTenantId(), project.getId());
        int outcomeCount = (int) outcomeRepository.countByTenantIdAndProjectId(project.getTenantId(), project.getId());
        int hypothesisCount = (int) hypothesisRepository.countByTenantIdAndProjectId(project.getTenantId(), project.getId());
        int teamMemberCount = 0;

        return projectMapper.toResponse(project, decisionCount, outcomeCount, hypothesisCount, teamMemberCount);
    }
}
