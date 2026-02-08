package ai.zevaro.core.domain.user;

import ai.zevaro.core.domain.rbac.RoleCategory;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<User> findByEmail(String email);

    List<User> findByTenantId(UUID tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.role.category = :category")
    List<User> findByTenantIdAndRoleCategory(@Param("tenantId") UUID tenantId, @Param("category") RoleCategory category);

    List<User> findByTenantIdAndDepartment(UUID tenantId, String department);

    List<User> findByManagerId(UUID managerId);

    List<User> findByManagerIdAndTenantId(UUID managerId, UUID tenantId);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.refreshToken = :token WHERE u.id = :userId")
    void updateRefreshToken(@Param("userId") UUID userId, @Param("token") String token);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :time WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("time") Instant time);
}
