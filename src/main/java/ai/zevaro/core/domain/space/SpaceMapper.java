package ai.zevaro.core.domain.space;

import ai.zevaro.core.domain.space.dto.CreateSpaceRequest;
import ai.zevaro.core.domain.space.dto.SpaceResponse;
import ai.zevaro.core.domain.space.dto.UpdateSpaceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SpaceMapper {

    public Space toEntity(CreateSpaceRequest request, UUID tenantId, UUID ownerId, String slug) {
        Space space = new Space();
        space.setTenantId(tenantId);
        space.setName(request.name());
        space.setSlug(slug);
        space.setDescription(request.description());
        space.setType(request.type());
        space.setStatus(SpaceStatus.ACTIVE);
        space.setProgramId(request.type() == SpaceType.PROGRAM ? request.programId() : null);
        space.setOwnerId(ownerId);
        space.setIcon(request.icon());
        space.setVisibility(request.visibility() != null ? request.visibility() : SpaceVisibility.PUBLIC);
        space.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        space.setCreatedById(ownerId);
        return space;
    }

    public SpaceResponse toResponse(Space space, String programName, String ownerName, int documentCount) {
        if (space == null) {
            return null;
        }

        return new SpaceResponse(
                space.getId(),
                space.getName(),
                space.getSlug(),
                space.getDescription(),
                space.getType(),
                space.getStatus(),
                space.getProgramId(),
                programName,
                space.getOwnerId(),
                ownerName,
                space.getIcon(),
                space.getVisibility(),
                space.getSortOrder(),
                documentCount,
                space.getCreatedAt(),
                space.getUpdatedAt()
        );
    }

    public void applyUpdate(Space existing, UpdateSpaceRequest request) {
        if (request.name() != null) {
            existing.setName(request.name());
        }
        if (request.description() != null) {
            existing.setDescription(request.description());
        }
        if (request.status() != null) {
            existing.setStatus(request.status());
        }
        if (request.visibility() != null) {
            existing.setVisibility(request.visibility());
        }
        if (request.icon() != null) {
            existing.setIcon(request.icon());
        }
        if (request.sortOrder() != null) {
            existing.setSortOrder(request.sortOrder());
        }
        // DO NOT allow type or programId to change
    }
}
