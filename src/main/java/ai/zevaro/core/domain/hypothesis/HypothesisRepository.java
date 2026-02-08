package ai.zevaro.core.domain.hypothesis;

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
public interface HypothesisRepository extends JpaRepository<Hypothesis, UUID> {

    List<Hypothesis> findByTenantId(UUID tenantId);

    Optional<Hypothesis> findByIdAndTenantId(UUID id, UUID tenantId);

    // JOIN FETCH for avoiding N+1
    @Query("SELECT h FROM Hypothesis h " +
           "LEFT JOIN FETCH h.owner " +
           "LEFT JOIN FETCH h.outcome " +
           "WHERE h.id = :id AND h.tenantId = :tenantId")
    Optional<Hypothesis> findByIdAndTenantIdWithDetails(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<Hypothesis> findByTenantIdAndStatus(UUID tenantId, HypothesisStatus status);

    List<Hypothesis> findByTenantIdAndPriority(UUID tenantId, HypothesisPriority priority);

    // Paginated queries
    Page<Hypothesis> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Hypothesis> findByTenantIdAndStatus(UUID tenantId, HypothesisStatus status, Pageable pageable);

    Page<Hypothesis> findByTenantIdAndPriority(UUID tenantId, HypothesisPriority priority, Pageable pageable);

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

    // Project-scoped queries
    Page<Hypothesis> findByTenantIdAndProgramId(UUID tenantId, UUID projectId, Pageable pageable);

    List<Hypothesis> findByTenantIdAndProgramId(UUID tenantId, UUID projectId);

    List<Hypothesis> findByTenantIdAndProgramIdAndStatus(UUID tenantId, UUID projectId, HypothesisStatus status);

    @Query("SELECT COUNT(h) FROM Hypothesis h WHERE h.tenantId = :tenantId AND h.program.id = :projectId")
    long countByTenantIdAndProjectId(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    @Query("SELECT h.status, COUNT(h) FROM Hypothesis h WHERE h.tenantId = :tenantId AND h.program.id = :projectId GROUP BY h.status")
    List<Object[]> countByStatusForProject(@Param("tenantId") UUID tenantId, @Param("projectId") UUID projectId);

    // For team workload - count hypotheses owned by user
    @Query("SELECT COUNT(h) FROM Hypothesis h WHERE h.tenantId = :tenantId AND h.owner.id = :userId")
    long countOwnedByUser(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);

    @Query("SELECT h FROM Hypothesis h WHERE h.tenantId = :tenantId AND (LOWER(h.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(h.belief) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Hypothesis> search(@Param("tenantId") UUID tenantId, @Param("query") String query, Pageable pageable);
}
