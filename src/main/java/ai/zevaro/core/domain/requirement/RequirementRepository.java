package ai.zevaro.core.domain.requirement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, UUID> {

    List<Requirement> findByTenantIdAndSpecificationIdOrderBySortOrderAsc(UUID tenantId, UUID specificationId);

    Page<Requirement> findByTenantIdAndSpecificationId(UUID tenantId, UUID specificationId, Pageable pageable);

    Optional<Requirement> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Requirement> findByTenantIdAndWorkstreamId(UUID tenantId, UUID workstreamId);

    List<Requirement> findByTenantIdAndProgramId(UUID tenantId, UUID programId);

    List<Requirement> findByTenantIdAndStatus(UUID tenantId, RequirementStatus status);

    List<Requirement> findByTenantIdAndSpecificationIdAndStatus(UUID tenantId, UUID specificationId, RequirementStatus status);

    long countByTenantIdAndSpecificationId(UUID tenantId, UUID specificationId);

    long countByTenantIdAndSpecificationIdAndStatus(UUID tenantId, UUID specificationId, RequirementStatus status);

    @Query("SELECT MAX(CAST(SUBSTRING(r.identifier, 5) AS int)) FROM Requirement r WHERE r.tenantId = :tenantId AND r.specificationId = :specificationId")
    Optional<Integer> findMaxIdentifierNumber(@Param("tenantId") UUID tenantId, @Param("specificationId") UUID specificationId);
}
