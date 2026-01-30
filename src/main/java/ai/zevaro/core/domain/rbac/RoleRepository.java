package ai.zevaro.core.domain.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    Optional<Role> findByCodeAndTenantIdIsNull(String code);

    Optional<Role> findByCodeAndTenantId(String code, UUID tenantId);

    List<Role> findByTenantIdIsNull();

    List<Role> findByTenantIdIsNullOrTenantId(UUID tenantId);

    List<Role> findByCategory(RoleCategory category);

    List<Role> findByCategoryAndTenantIdIsNull(RoleCategory category);
}
