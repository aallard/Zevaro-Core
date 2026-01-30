package ai.zevaro.core.domain.stakeholder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StakeholderRepository extends JpaRepository<Stakeholder, UUID> {

    Optional<Stakeholder> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Stakeholder> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<Stakeholder> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    List<Stakeholder> findByTenantId(UUID tenantId);

    List<Stakeholder> findByTenantIdAndActiveTrue(UUID tenantId);

    List<Stakeholder> findByTenantIdAndType(UUID tenantId, StakeholderType type);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    @Query("SELECT s FROM Stakeholder s WHERE s.tenantId = :tenantId AND s.decisionsPending > 0 ORDER BY s.decisionsPending DESC")
    List<Stakeholder> findWithPendingDecisions(@Param("tenantId") UUID tenantId);

    @Query("SELECT s FROM Stakeholder s WHERE s.tenantId = :tenantId AND s.decisionsCompleted > 0 AND s.avgResponseTimeHours IS NOT NULL ORDER BY s.avgResponseTimeHours ASC")
    List<Stakeholder> findFastestResponders(@Param("tenantId") UUID tenantId);

    @Query("SELECT s FROM Stakeholder s WHERE s.tenantId = :tenantId AND s.avgResponseTimeHours > :threshold")
    List<Stakeholder> findSlowResponders(@Param("tenantId") UUID tenantId, @Param("threshold") Double threshold);

    @Query("SELECT s FROM Stakeholder s WHERE s.tenantId = :tenantId AND s.expertise LIKE %:expertise%")
    List<Stakeholder> findByExpertiseContaining(@Param("tenantId") UUID tenantId, @Param("expertise") String expertise);

    @Modifying
    @Query("UPDATE Stakeholder s SET s.decisionsPending = s.decisionsPending + 1 WHERE s.id = :id")
    void incrementPendingDecisions(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Stakeholder s SET s.decisionsPending = CASE WHEN s.decisionsPending > 0 THEN s.decisionsPending - 1 ELSE 0 END, s.decisionsCompleted = s.decisionsCompleted + 1, s.lastDecisionAt = :decidedAt WHERE s.id = :id")
    void recordDecisionCompleted(@Param("id") UUID id, @Param("decidedAt") Instant decidedAt);

    @Modifying
    @Query("UPDATE Stakeholder s SET s.decisionsEscalated = s.decisionsEscalated + 1 WHERE s.id = :id")
    void incrementEscalatedDecisions(@Param("id") UUID id);
}
