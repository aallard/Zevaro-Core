package ai.zevaro.core.domain.specification;

import ai.zevaro.core.domain.specification.dto.CreateSpecificationRequest;
import ai.zevaro.core.domain.specification.dto.SpecificationResponse;
import ai.zevaro.core.domain.specification.dto.UpdateSpecificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpecificationMapper {

    public Specification toEntity(CreateSpecificationRequest req, UUID workstreamId, UUID programId,
                                   UUID tenantId, UUID createdById) {
        Specification spec = new Specification();
        spec.setTenantId(tenantId);
        spec.setWorkstreamId(workstreamId);
        spec.setProgramId(programId);
        spec.setName(req.name());
        spec.setDescription(req.description());
        spec.setStatus(SpecificationStatus.DRAFT);
        spec.setVersion(1);
        spec.setAuthorId(createdById);
        spec.setReviewerId(req.reviewerId());
        spec.setEstimatedHours(req.estimatedHours());
        spec.setCreatedById(createdById);
        return spec;
    }

    public SpecificationResponse toResponse(Specification spec, String workstreamName, String programName,
                                             String authorName, String reviewerName, String approvedByName,
                                             int requirementCount) {
        return new SpecificationResponse(
                spec.getId(),
                spec.getWorkstreamId(),
                workstreamName,
                spec.getProgramId(),
                programName,
                spec.getName(),
                spec.getDescription(),
                spec.getDocumentId(),
                spec.getStatus(),
                spec.getVersion(),
                spec.getAuthorId(),
                authorName,
                spec.getReviewerId(),
                reviewerName,
                spec.getApprovedAt(),
                spec.getApprovedById(),
                approvedByName,
                spec.getEstimatedHours(),
                spec.getActualHours(),
                requirementCount,
                spec.getCreatedAt(),
                spec.getUpdatedAt()
        );
    }

    public void applyUpdate(Specification existing, UpdateSpecificationRequest req) {
        boolean contentChanged = false;

        if (req.name() != null) {
            if (!req.name().equals(existing.getName())) {
                contentChanged = true;
            }
            existing.setName(req.name());
        }
        if (req.description() != null) {
            if (!req.description().equals(existing.getDescription())) {
                contentChanged = true;
            }
            existing.setDescription(req.description());
        }
        if (req.reviewerId() != null) {
            existing.setReviewerId(req.reviewerId());
        }
        if (req.estimatedHours() != null) {
            existing.setEstimatedHours(req.estimatedHours());
        }
        if (req.actualHours() != null) {
            existing.setActualHours(req.actualHours());
        }

        // Auto-increment version if content changed after approval
        if (contentChanged && isApprovedOrLater(existing.getStatus())) {
            existing.setVersion(existing.getVersion() + 1);
        }
    }

    private boolean isApprovedOrLater(SpecificationStatus status) {
        return status == SpecificationStatus.APPROVED
                || status == SpecificationStatus.IN_PROGRESS
                || status == SpecificationStatus.DELIVERED
                || status == SpecificationStatus.ACCEPTED;
    }
}
