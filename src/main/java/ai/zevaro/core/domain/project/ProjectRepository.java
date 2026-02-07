package ai.zevaro.core.domain.project;

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
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByIdAndTenantId(UUID id, UUID tenantId);

    // JOIN FETCH for avoiding N+1
    @Query("SELECT p FROM Project p " +
           "LEFT JOIN FETCH p.owner " +
           "WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<Project> findByIdAndTenantIdWithDetails(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<Project> findByTenantId(UUID tenantId);

    // Paginated queries
    Page<Project> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Project> findByTenantIdAndStatus(UUID tenantId, ProjectStatus status, Pageable pageable);

    List<Project> findByTenantIdAndStatus(UUID tenantId, ProjectStatus status);

    Optional<Project> findBySlugAndTenantId(String slug, UUID tenantId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.tenantId = :tenantId AND p.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") ProjectStatus status);
}
