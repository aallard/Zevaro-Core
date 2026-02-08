package ai.zevaro.core.domain.link;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntityLinkRepository extends JpaRepository<EntityLink, UUID> {

    List<EntityLink> findByTenantIdAndSourceTypeAndSourceId(UUID tenantId, EntityType sourceType, UUID sourceId);

    List<EntityLink> findByTenantIdAndTargetTypeAndTargetId(UUID tenantId, EntityType targetType, UUID targetId);

    Optional<EntityLink> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsBySourceTypeAndSourceIdAndTargetTypeAndTargetIdAndLinkType(
            EntityType sourceType, UUID sourceId, EntityType targetType, UUID targetId, LinkType linkType);

    void deleteByIdAndTenantId(UUID id, UUID tenantId);
}
