package ai.zevaro.core.domain.team;

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
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Team> findBySlugAndTenantId(String slug, UUID tenantId);

    List<Team> findByTenantId(UUID tenantId);

    Page<Team> findByTenantId(UUID tenantId, Pageable pageable);

    List<Team> findByTenantIdAndActiveTrue(UUID tenantId);

    List<Team> findByLeadId(UUID leadId);

    boolean existsBySlugAndTenantId(String slug, UUID tenantId);

    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.user.id = :userId AND t.tenantId = :tenantId")
    List<Team> findByMemberUserId(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    // Project-scoped queries
    List<Team> findByTenantIdAndProjectId(UUID tenantId, UUID projectId);
}
