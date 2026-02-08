package ai.zevaro.core.domain.space;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpaceRepository extends JpaRepository<Space, UUID> {

    List<Space> findByTenantIdAndStatusOrderBySortOrderAsc(UUID tenantId, SpaceStatus status);

    List<Space> findByTenantIdOrderBySortOrderAsc(UUID tenantId);

    List<Space> findByTenantIdAndTypeAndStatus(UUID tenantId, SpaceType type, SpaceStatus status);

    Page<Space> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Space> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Space> findByTenantIdAndSlug(UUID tenantId, String slug);

    Optional<Space> findByTenantIdAndProgramId(UUID tenantId, UUID programId);

    boolean existsByTenantIdAndSlug(UUID tenantId, String slug);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    long countByTenantIdAndType(UUID tenantId, SpaceType type);
}
