package ai.zevaro.core.domain.ticket;

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
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByTenantIdAndWorkstreamIdOrderByCreatedAtDesc(UUID tenantId, UUID workstreamId);

    Page<Ticket> findByTenantIdAndWorkstreamId(UUID tenantId, UUID workstreamId, Pageable pageable);

    Optional<Ticket> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Ticket> findByTenantIdAndProgramId(UUID tenantId, UUID programId);

    List<Ticket> findByTenantIdAndStatus(UUID tenantId, TicketStatus status);

    List<Ticket> findByTenantIdAndSeverity(UUID tenantId, TicketSeverity severity);

    List<Ticket> findByTenantIdAndWorkstreamIdAndStatus(UUID tenantId, UUID workstreamId, TicketStatus status);

    List<Ticket> findByTenantIdAndAssignedToId(UUID tenantId, UUID assignedToId);

    long countByTenantIdAndWorkstreamId(UUID tenantId, UUID workstreamId);

    long countByTenantIdAndProgramIdAndStatus(UUID tenantId, UUID programId, TicketStatus status);

    long countByTenantIdAndWorkstreamIdAndSeverity(UUID tenantId, UUID workstreamId, TicketSeverity severity);

    @Query("SELECT MAX(CAST(SUBSTRING(t.identifier, LENGTH(:prefix) + 1) AS int)) FROM Ticket t WHERE t.tenantId = :tenantId AND t.workstreamId = :workstreamId AND t.identifier LIKE CONCAT(:prefix, '%')")
    Optional<Integer> findMaxIdentifierNumber(@Param("tenantId") UUID tenantId, @Param("workstreamId") UUID workstreamId, @Param("prefix") String prefix);
}
