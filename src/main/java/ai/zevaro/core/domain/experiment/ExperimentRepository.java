package ai.zevaro.core.domain.experiment;

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
public interface ExperimentRepository extends JpaRepository<Experiment, UUID> {

    Optional<Experiment> findByIdAndTenantId(UUID id, UUID tenantId);

    // JOIN FETCH for avoiding N+1
    @Query("SELECT e FROM Experiment e " +
           "LEFT JOIN FETCH e.hypothesis " +
           "LEFT JOIN FETCH e.project " +
           "LEFT JOIN FETCH e.owner " +
           "WHERE e.id = :id AND e.tenantId = :tenantId")
    Optional<Experiment> findByIdAndTenantIdWithDetails(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<Experiment> findByTenantId(UUID tenantId);

    // Paginated queries
    Page<Experiment> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Experiment> findByTenantIdAndStatus(UUID tenantId, ExperimentStatus status, Pageable pageable);

    Page<Experiment> findByTenantIdAndProjectId(UUID tenantId, UUID projectId, Pageable pageable);

    List<Experiment> findByTenantIdAndStatus(UUID tenantId, ExperimentStatus status);

    List<Experiment> findByTenantIdAndProjectId(UUID tenantId, UUID projectId);

    List<Experiment> findByTenantIdAndProjectIdAndStatus(UUID tenantId, UUID projectId, ExperimentStatus status);

    List<Experiment> findByHypothesisId(UUID hypothesisId);

    @Query("SELECT COUNT(e) FROM Experiment e WHERE e.tenantId = :tenantId AND e.project.id = :projectId")
    long countByTenantIdAndProjectId(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    @Query("SELECT COUNT(e) FROM Experiment e WHERE e.tenantId = :tenantId AND e.project.id = :projectId AND e.status = :status")
    long countByTenantIdAndProjectIdAndStatus(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId, @Param("status") ExperimentStatus status);

    // For dashboard - count running experiments
    @Query("SELECT COUNT(e) FROM Experiment e WHERE e.tenantId = :tenantId AND e.project.id = :projectId AND e.status IN ('RUNNING', 'IN_PROGRESS', 'ACTIVE')")
    long countRunningForProject(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);
}
