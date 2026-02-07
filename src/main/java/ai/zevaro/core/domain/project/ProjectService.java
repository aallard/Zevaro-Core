package ai.zevaro.core.domain.project;

import ai.zevaro.core.domain.decision.DecisionRepository;
import ai.zevaro.core.domain.hypothesis.HypothesisRepository;
import ai.zevaro.core.domain.outcome.OutcomeRepository;
import ai.zevaro.core.domain.project.dto.CreateProjectRequest;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OutcomeRepository outcomeRepository;
    private final HypothesisRepository hypothesisRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final EventPublisher eventPublisher;

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

    private ProjectResponse toResponseWithCounts(Project project) {
        int decisionCount = (int) decisionRepository.countByTenantIdAndProjectId(project.getTenantId(), project.getId());
        int outcomeCount = (int) outcomeRepository.countByTenantIdAndProjectId(project.getTenantId(), project.getId());
        int hypothesisCount = (int) hypothesisRepository.countByTenantIdAndProjectId(project.getTenantId(), project.getId());
        int teamMemberCount = 0;

        return projectMapper.toResponse(project, decisionCount, outcomeCount, hypothesisCount, teamMemberCount);
    }
}
