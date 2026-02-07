package ai.zevaro.core.domain.project;

import ai.zevaro.core.domain.project.dto.CreateProjectRequest;
import ai.zevaro.core.domain.project.dto.ProjectResponse;
import ai.zevaro.core.domain.project.dto.ProjectSummary;
import ai.zevaro.core.domain.project.dto.UpdateProjectRequest;
import ai.zevaro.core.domain.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectMapper {

    private final UserMapper userMapper;

    public ProjectResponse toResponse(Project project, int decisionCount, int outcomeCount,
                                       int hypothesisCount, int teamMemberCount) {
        if (project == null) {
            return null;
        }

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getStatus(),
                project.getColor(),
                project.getIconUrl(),
                project.getOwner() != null ? userMapper.toSummary(project.getOwner()) : null,
                decisionCount,
                outcomeCount,
                hypothesisCount,
                teamMemberCount,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public ProjectSummary toSummary(Project project) {
        if (project == null) {
            return null;
        }

        return new ProjectSummary(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getStatus(),
                project.getColor()
        );
    }

    public Project toEntity(CreateProjectRequest request, UUID tenantId, UUID createdById) {
        Project project = new Project();
        project.setTenantId(tenantId);
        project.setName(request.name());
        project.setSlug(generateSlug(request.name()));
        project.setDescription(request.description());
        project.setStatus(request.status() != null ? request.status() : ProjectStatus.ACTIVE);
        project.setColor(request.color());
        project.setCreatedById(createdById);
        return project;
    }

    public void updateEntity(Project project, UpdateProjectRequest request) {
        if (request.name() != null) {
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.status() != null) {
            project.setStatus(request.status());
        }
        if (request.color() != null) {
            project.setColor(request.color());
        }
        if (request.iconUrl() != null) {
            project.setIconUrl(request.iconUrl());
        }
    }

    public String generateSlug(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        return name
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-+", "-");
    }
}
