package ai.zevaro.core.domain.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByTenantIdOrderByTimestampDesc(UUID tenantId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndActorIdOrderByTimestampDesc(UUID tenantId, UUID actorId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndEntityTypeAndEntityIdOrderByTimestampDesc(
            UUID tenantId, String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findByTenantIdAndActionOrderByTimestampDesc(UUID tenantId, AuditAction action, Pageable pageable);

    Page<AuditLog> findByTenantIdAndEntityTypeOrderByTimestampDesc(UUID tenantId, String entityType, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.tenantId = :tenantId AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTenantIdAndTimestampBetween(
            @Param("tenantId") UUID tenantId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.tenantId = :tenantId AND a.timestamp > :since GROUP BY a.action")
    List<Object[]> countByActionSince(@Param("tenantId") UUID tenantId, @Param("since") Instant since);

    // For dashboard - recent activity for a tenant (needs pageable or native query)
    @Query(value = "SELECT * FROM audit_logs WHERE tenant_id = :tenantId ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<AuditLog> findRecentActivityForTenant(@Param("tenantId") UUID tenantId, @Param("limit") int limit);
}
