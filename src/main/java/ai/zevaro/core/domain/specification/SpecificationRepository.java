package ai.zevaro.core.domain.specification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, UUID> {

    List<Specification> findByTenantIdAndWorkstreamIdOrderByCreatedAtDesc(UUID tenantId, UUID workstreamId);

    Page<Specification> findByTenantIdAndWorkstreamId(UUID tenantId, UUID workstreamId, Pageable pageable);

    Optional<Specification> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Specification> findByTenantIdAndProgramId(UUID tenantId, UUID programId);

    List<Specification> findByTenantIdAndStatus(UUID tenantId, SpecificationStatus status);

    List<Specification> findByTenantIdAndWorkstreamIdAndStatus(UUID tenantId, UUID workstreamId, SpecificationStatus status);

    List<Specification> findByTenantIdAndReviewerIdAndStatus(UUID tenantId, UUID reviewerId, SpecificationStatus status);

    long countByTenantIdAndWorkstreamId(UUID tenantId, UUID workstreamId);

    long countByTenantIdAndProgramIdAndStatus(UUID tenantId, UUID programId, SpecificationStatus status);
}
