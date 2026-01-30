package ai.zevaro.core.domain.hypothesis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HypothesisRepository extends JpaRepository<Hypothesis, UUID> {

    List<Hypothesis> findByTenantId(UUID tenantId);

    Optional<Hypothesis> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Hypothesis> findByTenantIdAndStatus(UUID tenantId, HypothesisStatus status);

    List<Hypothesis> findByTenantIdAndPriority(UUID tenantId, HypothesisPriority priority);

    @Query("SELECT h FROM Hypothesis h WHERE h.tenantId = :tenantId AND h.outcome.id = :outcomeId")
    List<Hypothesis> findByTenantIdAndOutcomeId(@Param("tenantId") UUID tenantId, @Param("outcomeId") UUID outcomeId);

    @Query("SELECT h FROM Hypothesis h WHERE h.tenantId = :tenantId AND h.owner.id = :ownerId")
    List<Hypothesis> findByTenantIdAndOwnerId(@Param("tenantId") UUID tenantId, @Param("ownerId") UUID ownerId);

    @Query("SELECT h FROM Hypothesis h WHERE h.tenantId = :tenantId AND h.status = 'BLOCKED'")
    List<Hypothesis> findBlockedByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT h FROM Hypothesis h WHERE h.tenantId = :tenantId AND h.status IN ('BUILDING', 'DEPLOYED', 'MEASURING')")
    List<Hypothesis> findActiveByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(h) FROM Hypothesis h WHERE h.outcome.id = :outcomeId")
    long countByOutcomeId(@Param("outcomeId") UUID outcomeId);

    @Query("SELECT h.status, COUNT(h) FROM Hypothesis h WHERE h.tenantId = :tenantId GROUP BY h.status")
    List<Object[]> countByStatusForTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT h.status, COUNT(h) FROM Hypothesis h WHERE h.outcome.id = :outcomeId GROUP BY h.status")
    List<Object[]> countByStatusForOutcome(@Param("outcomeId") UUID outcomeId);

    boolean existsByOutcomeIdAndTenantId(UUID outcomeId, UUID tenantId);
}
