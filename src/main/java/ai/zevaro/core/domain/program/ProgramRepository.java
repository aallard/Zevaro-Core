package ai.zevaro.core.domain.program;

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
public interface ProgramRepository extends JpaRepository<Program, UUID> {

    Optional<Program> findByIdAndTenantId(UUID id, UUID tenantId);

    // JOIN FETCH for avoiding N+1
    @Query("SELECT p FROM Program p " +
           "LEFT JOIN FETCH p.owner " +
           "WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Program> findByIdAndTenantIdWithDetails(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<Program> findByTenantId(UUID tenantId);

    // Paginated queries
    Page<Program> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Program> findByTenantIdAndStatus(UUID tenantId, ProgramStatus status, Pageable pageable);

    List<Program> findByTenantIdAndStatus(UUID tenantId, ProgramStatus status);

    Optional<Program> findBySlugAndTenantId(String slug, UUID tenantId);

    @Query("SELECT COUNT(p) FROM Program p WHERE p.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(p) FROM Program p WHERE p.tenantId = :tenantId AND p.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") ProgramStatus status);

    List<Program> findByTenantIdAndPortfolioId(UUID tenantId, UUID portfolioId);

    @Query("SELECT p FROM Program p WHERE p.tenantId = :tenantId AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Program> search(@Param("tenantId") UUID tenantId, @Param("query") String query, Pageable pageable);
}
