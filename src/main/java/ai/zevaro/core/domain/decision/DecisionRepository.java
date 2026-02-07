package ai.zevaro.core.domain.decision;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, UUID> {

    Optional<Decision> findByIdAndTenantId(UUID id, UUID tenantId);

    // JOIN FETCH queries for avoiding N+1
    @Query("SELECT d FROM Decision d " +
           "LEFT JOIN FETCH d.owner " +
           "LEFT JOIN FETCH d.assignedTo " +
           "LEFT JOIN FETCH d.team " +
           "WHERE d.id = :id AND d.tenantId = :tenantId")
    Optional<Decision> findByIdAndTenantIdWithDetails(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("SELECT DISTINCT d FROM Decision d " +
           "LEFT JOIN FETCH d.owner " +
           "LEFT JOIN FETCH d.assignedTo " +
           "LEFT JOIN FETCH d.team " +
           "WHERE d.tenantId = :tenantId")
    List<Decision> findByTenantIdWithDetails(@Param("tenantId") UUID tenantId);

    @Query("SELECT DISTINCT d FROM Decision d " +
           "LEFT JOIN FETCH d.owner " +
           "LEFT JOIN FETCH d.assignedTo " +
           "WHERE d.tenantId = :tenantId AND d.status = :status")
    List<Decision> findByTenantIdAndStatusWithDetails(@Param("tenantId") UUID tenantId, @Param("status") DecisionStatus status);

    List<Decision> findByTenantId(UUID tenantId);

    // Paginated queries
    Page<Decision> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Decision> findByTenantIdAndStatus(UUID tenantId, DecisionStatus status, Pageable pageable);

    Page<Decision> findByTenantIdAndPriority(UUID tenantId, DecisionPriority priority, Pageable pageable);

    Page<Decision> findByTenantIdAndDecisionType(UUID tenantId, DecisionType type, Pageable pageable);

    Page<Decision> findByTeamId(UUID teamId, Pageable pageable);

    List<Decision> findByTenantIdAndStatus(UUID tenantId, DecisionStatus status);

    List<Decision> findByTenantIdAndStatusIn(UUID tenantId, List<DecisionStatus> statuses);

    @Query("SELECT d FROM Decision d WHERE d.tenantId = :tenantId AND d.status IN :statuses AND d.team.id = :teamId")
    List<Decision> findByTenantIdAndStatusInAndTeamId(@Param("tenantId") UUID tenantId,
                                                       @Param("statuses") List<DecisionStatus> statuses,
                                                       @Param("teamId") UUID teamId);

    List<Decision> findByTenantIdAndPriorityAndStatusIn(UUID tenantId, DecisionPriority priority, List<DecisionStatus> statuses);

    List<Decision> findByTenantIdAndPriority(UUID tenantId, DecisionPriority priority);

    List<Decision> findByTenantIdAndDecisionType(UUID tenantId, DecisionType type);

    List<Decision> findByOwnerId(UUID ownerId);

    List<Decision> findByAssignedToId(UUID assignedToId);

    List<Decision> findByTenantIdAndAssignedToIdAndStatusIn(UUID tenantId, UUID assignedToId, List<DecisionStatus> statuses);

    List<Decision> findByTenantIdAndAssignedToId(UUID tenantId, UUID assignedToId);

    List<Decision> findByOutcomeId(UUID outcomeId);

    List<Decision> findByHypothesisId(UUID hypothesisId);

    List<Decision> findByTeamId(UUID teamId);

    @Query("SELECT d FROM Decision d WHERE d.tenantId = :tenantId AND d.status IN :statuses ORDER BY d.priority, d.createdAt")
    List<Decision> findDecisionQueue(@Param("tenantId") UUID tenantId, @Param("statuses") List<DecisionStatus> statuses);

    @Query("SELECT d FROM Decision d WHERE d.tenantId = :tenantId AND d.dueAt < :now AND d.status IN ('NEEDS_INPUT', 'UNDER_DISCUSSION')")
    List<Decision> findOverdueDecisions(@Param("tenantId") UUID tenantId, @Param("now") Instant now);

    @Query("SELECT d FROM Decision d WHERE d.tenantId = :tenantId AND d.dueAt < :now AND d.status IN ('NEEDS_INPUT', 'UNDER_DISCUSSION') AND d.escalationLevel = 0")
    List<Decision> findNeedingEscalation(@Param("tenantId") UUID tenantId, @Param("now") Instant now);

    @Query("SELECT d FROM Decision d WHERE d.assignedTo.id = :userId AND d.status IN ('NEEDS_INPUT', 'UNDER_DISCUSSION') ORDER BY d.priority, d.createdAt")
    List<Decision> findMyPendingDecisions(@Param("userId") UUID userId);

    @Query("SELECT d.status, COUNT(d) FROM Decision d WHERE d.tenantId = :tenantId GROUP BY d.status")
    List<Object[]> countByStatusForTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(d) FROM Decision d WHERE d.tenantId = :tenantId AND d.status IN ('NEEDS_INPUT', 'UNDER_DISCUSSION')")
    long countPendingDecisions(@Param("tenantId") UUID tenantId);

    @Query("SELECT d FROM Decision d WHERE d.tenantId = :tenantId AND d.decidedAt BETWEEN :start AND :end")
    List<Decision> findResolvedBetween(@Param("tenantId") UUID tenantId, @Param("start") Instant start, @Param("end") Instant end);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (decided_at - created_at))/3600) FROM core.decisions WHERE tenant_id = :tenantId AND decided_at IS NOT NULL AND decided_at > :since", nativeQuery = true)
    Double getAverageDecisionTimeHours(@Param("tenantId") UUID tenantId, @Param("since") Instant since);

    // Project-scoped queries
    Page<Decision> findByTenantIdAndProjectId(UUID tenantId, UUID projectId, Pageable pageable);

    List<Decision> findByTenantIdAndProjectId(UUID tenantId, UUID projectId);

    List<Decision> findByTenantIdAndProjectIdAndStatus(UUID tenantId, UUID projectId, DecisionStatus status);

    @Query("SELECT COUNT(d) FROM Decision d WHERE d.tenantId = :tenantId AND d.project.id = :projectId")
    long countByTenantIdAndProjectId(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    @Query("SELECT COUNT(d) FROM Decision d WHERE d.tenantId = :tenantId AND d.project.id = :projectId AND d.status = :status")
    long countByTenantIdAndProjectIdAndStatus(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId, @Param("status") DecisionStatus status);

    @Query("SELECT d.status, COUNT(d) FROM Decision d WHERE d.tenantId = :tenantId AND d.project.id = :projectId GROUP BY d.status")
    List<Object[]> countByStatusForProject(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);
}
