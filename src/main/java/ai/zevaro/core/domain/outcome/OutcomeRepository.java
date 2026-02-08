package ai.zevaro.core.domain.outcome;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutcomeRepository extends JpaRepository<Outcome, UUID> {

    Optional<Outcome> findByIdAndTenantId(UUID id, UUID tenantId);

    // JOIN FETCH for avoiding N+1
    @Query("SELECT o FROM Outcome o " +
           "LEFT JOIN FETCH o.owner " +
           "LEFT JOIN FETCH o.team " +
           "WHERE o.id = :id AND o.tenantId = :tenantId")
    Optional<Outcome> findByIdAndTenantIdWithDetails(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<Outcome> findByTenantId(UUID tenantId);

    // Paginated queries
    Page<Outcome> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Outcome> findByTenantIdAndStatus(UUID tenantId, OutcomeStatus status, Pageable pageable);

    Page<Outcome> findByTenantIdAndTeamId(UUID tenantId, UUID teamId, Pageable pageable);

    Page<Outcome> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId, Pageable pageable);

    Page<Outcome> findByTenantIdAndPriority(UUID tenantId, OutcomePriority priority, Pageable pageable);

    List<Outcome> findByTenantIdAndStatus(UUID tenantId, OutcomeStatus status);

    List<Outcome> findByTenantIdAndStatusIn(UUID tenantId, List<OutcomeStatus> statuses);

    List<Outcome> findByTenantIdAndTeamId(UUID tenantId, UUID teamId);

    List<Outcome> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId);

    List<Outcome> findByTenantIdAndPriority(UUID tenantId, OutcomePriority priority);

    @Query("SELECT o FROM Outcome o WHERE o.tenantId = :tenantId AND o.status IN :statuses ORDER BY o.priority, o.targetDate")
    List<Outcome> findActiveOutcomes(@Param("tenantId") UUID tenantId, @Param("statuses") List<OutcomeStatus> statuses);

    @Query("SELECT o FROM Outcome o WHERE o.tenantId = :tenantId AND o.targetDate < :date AND o.status NOT IN ('VALIDATED', 'INVALIDATED', 'ABANDONED')")
    List<Outcome> findOverdueOutcomes(@Param("tenantId") UUID tenantId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(o) FROM Outcome o WHERE o.tenantId = :tenantId AND o.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") OutcomeStatus status);

    @Query("SELECT o.status, COUNT(o) FROM Outcome o WHERE o.tenantId = :tenantId GROUP BY o.status")
    List<Object[]> countByStatusForTenant(@Param("tenantId") UUID tenantId);

    // Project-scoped queries
    Page<Outcome> findByTenantIdAndProgramId(UUID tenantId, UUID projectId, Pageable pageable);

    List<Outcome> findByTenantIdAndProgramId(UUID tenantId, UUID projectId);

    List<Outcome> findByTenantIdAndProgramIdAndStatus(UUID tenantId, UUID projectId, OutcomeStatus status);

    @Query("SELECT COUNT(o) FROM Outcome o WHERE o.tenantId = :tenantId AND o.program.id = :projectId")
    long countByTenantIdAndProjectId(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    @Query("SELECT COUNT(o) FROM Outcome o WHERE o.tenantId = :tenantId AND o.program.id = :projectId AND o.status = :status")
    long countByTenantIdAndProjectIdAndStatus(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId, @Param("status") OutcomeStatus status);

    @Query("SELECT o.status, COUNT(o) FROM Outcome o WHERE o.tenantId = :tenantId AND o.program.id = :projectId GROUP BY o.status")
    List<Object[]> countByStatusForProject(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    // For dashboard - count validated outcomes
    @Query("SELECT COUNT(o) FROM Outcome o WHERE o.tenantId = :tenantId AND o.program.id = :projectId AND o.status = 'VALIDATED'")
    long countValidatedForProject(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    // For dashboard - count non-draft outcomes
    @Query("SELECT COUNT(o) FROM Outcome o WHERE o.tenantId = :tenantId AND o.program.id = :projectId AND o.status != 'DRAFT'")
    long countNonDraftForProject(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);
}
