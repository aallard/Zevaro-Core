package ai.zevaro.core.domain.outcome;

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

    List<Outcome> findByTenantId(UUID tenantId);

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
}
