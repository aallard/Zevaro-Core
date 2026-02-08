package ai.zevaro.core.domain.portfolio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

    List<Portfolio> findByTenantId(UUID tenantId);

    List<Portfolio> findByTenantIdAndStatus(UUID tenantId, PortfolioStatus status);

    Page<Portfolio> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Portfolio> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Portfolio> findByTenantIdAndSlug(UUID tenantId, String slug);

    boolean existsByTenantIdAndSlug(UUID tenantId, String slug);

    long countByTenantIdAndStatus(UUID tenantId, PortfolioStatus status);
}
