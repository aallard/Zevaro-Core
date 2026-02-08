package ai.zevaro.core.domain.template;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramTemplateRepository extends JpaRepository<ProgramTemplate, UUID> {

    List<ProgramTemplate> findByTenantIdOrIsSystemTrue(UUID tenantId);

    List<ProgramTemplate> findByIsSystemTrue();

    @Query("SELECT t FROM ProgramTemplate t WHERE t.id = :id AND (t.tenantId = :tenantId OR t.isSystem = true)")
    Optional<ProgramTemplate> findByIdAccessible(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    boolean existsByName(String name);
}
