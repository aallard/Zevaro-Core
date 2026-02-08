package ai.zevaro.core.domain.workstream;

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
public interface WorkstreamRepository extends JpaRepository<Workstream, UUID> {

    List<Workstream> findByTenantIdAndProgramIdOrderBySortOrderAsc(UUID tenantId, UUID programId);

    Page<Workstream> findByTenantIdAndProgramId(UUID tenantId, UUID programId, Pageable pageable);

    Optional<Workstream> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Workstream> findByTenantIdAndStatus(UUID tenantId, WorkstreamStatus status);

    List<Workstream> findByTenantIdAndProgramIdAndMode(UUID tenantId, UUID programId, WorkstreamMode mode);

    long countByTenantIdAndProgramId(UUID tenantId, UUID programId);

    boolean existsByTenantIdAndProgramIdAndName(UUID tenantId, UUID programId, String name);

    @Query("SELECT w FROM Workstream w WHERE w.tenantId = :tenantId AND (LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(w.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Workstream> search(@Param("tenantId") UUID tenantId, @Param("query") String query, Pageable pageable);
}
