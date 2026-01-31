package ai.zevaro.core.domain.queue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionQueueRepository extends JpaRepository<DecisionQueue, UUID> {

    List<DecisionQueue> findByTenantId(UUID tenantId);

    Optional<DecisionQueue> findByTenantIdAndIsDefaultTrue(UUID tenantId);

    Optional<DecisionQueue> findByTenantIdAndName(UUID tenantId, String name);

    Optional<DecisionQueue> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndName(UUID tenantId, String name);
}
