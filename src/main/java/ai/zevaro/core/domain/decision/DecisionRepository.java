package ai.zevaro.core.domain.decision;

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

    List<Decision> findByTenantId(UUID tenantId);

    List<Decision> findByTenantIdAndStatus(UUID tenantId, DecisionStatus status);

    List<Decision> findByTenantIdAndStatusIn(UUID tenantId, List<DecisionStatus> statuses);

    List<Decision> findByTenantIdAndPriority(UUID tenantId, DecisionPriority priority);

    List<Decision> findByTenantIdAndDecisionType(UUID tenantId, DecisionType type);

    List<Decision> findByOwnerId(UUID ownerId);

    List<Decision> findByAssignedToId(UUID assignedToId);

    List<Decision> findByTenantIdAndAssignedToIdAndStatusIn(UUID tenantId, UUID assignedToId, List<DecisionStatus> statuses);

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

    @Query("SELECT AVG(EXTRACT(EPOCH FROM (d.decidedAt - d.createdAt))/3600) FROM Decision d WHERE d.tenantId = :tenantId AND d.decidedAt IS NOT NULL AND d.decidedAt > :since")
    Double getAverageDecisionTimeHours(@Param("tenantId") UUID tenantId, @Param("since") Instant since);
}
