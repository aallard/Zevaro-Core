package ai.zevaro.core.domain.requirement;

import ai.zevaro.core.domain.requirement.dto.CreateRequirementRequest;
import ai.zevaro.core.domain.requirement.dto.DependencyResponse;
import ai.zevaro.core.domain.requirement.dto.RequirementResponse;
import ai.zevaro.core.domain.requirement.dto.UpdateRequirementRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RequirementMapper {

    public Requirement toEntity(CreateRequirementRequest req, UUID specificationId, UUID workstreamId,
                                 UUID programId, UUID tenantId, UUID createdById, String identifier) {
        Requirement r = new Requirement();
        r.setTenantId(tenantId);
        r.setSpecificationId(specificationId);
        r.setWorkstreamId(workstreamId);
        r.setProgramId(programId);
        r.setIdentifier(identifier);
        r.setTitle(req.title());
        r.setDescription(req.description());
        r.setType(req.type());
        r.setPriority(req.priority());
        r.setStatus(RequirementStatus.DRAFT);
        r.setAcceptanceCriteria(req.acceptanceCriteria());
        r.setEstimatedHours(req.estimatedHours());
        r.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        r.setCreatedById(createdById);
        return r;
    }

    public RequirementResponse toResponse(Requirement req, String specificationName,
                                           List<DependencyResponse> deps, List<DependencyResponse> dependedOnBy) {
        return new RequirementResponse(
                req.getId(),
                req.getSpecificationId(),
                specificationName,
                req.getWorkstreamId(),
                req.getProgramId(),
                req.getIdentifier(),
                req.getTitle(),
                req.getDescription(),
                req.getType(),
                req.getPriority(),
                req.getStatus(),
                req.getAcceptanceCriteria(),
                req.getEstimatedHours(),
                req.getActualHours(),
                req.getSortOrder(),
                deps,
                dependedOnBy,
                req.getCreatedAt(),
                req.getUpdatedAt()
        );
    }

    public void applyUpdate(Requirement existing, UpdateRequirementRequest req) {
        if (req.title() != null) {
            existing.setTitle(req.title());
        }
        if (req.description() != null) {
            existing.setDescription(req.description());
        }
        if (req.type() != null) {
            existing.setType(req.type());
        }
        if (req.priority() != null) {
            existing.setPriority(req.priority());
        }
        if (req.status() != null) {
            existing.setStatus(req.status());
        }
        if (req.acceptanceCriteria() != null) {
            existing.setAcceptanceCriteria(req.acceptanceCriteria());
        }
        if (req.estimatedHours() != null) {
            existing.setEstimatedHours(req.estimatedHours());
        }
        if (req.actualHours() != null) {
            existing.setActualHours(req.actualHours());
        }
        if (req.sortOrder() != null) {
            existing.setSortOrder(req.sortOrder());
        }
    }

    public DependencyResponse toDependencyResponse(RequirementDependency dep, Requirement source, Requirement target) {
        return new DependencyResponse(
                dep.getId(),
                dep.getRequirementId(),
                source != null ? source.getIdentifier() : null,
                source != null ? source.getTitle() : null,
                dep.getDependsOnId(),
                target != null ? target.getIdentifier() : null,
                target != null ? target.getTitle() : null,
                dep.getType(),
                dep.getCreatedAt()
        );
    }
}
