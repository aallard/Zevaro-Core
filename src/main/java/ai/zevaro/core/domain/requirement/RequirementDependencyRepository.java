package ai.zevaro.core.domain.requirement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequirementDependencyRepository extends JpaRepository<RequirementDependency, UUID> {

    List<RequirementDependency> findByRequirementId(UUID requirementId);

    List<RequirementDependency> findByDependsOnId(UUID dependsOnId);

    Optional<RequirementDependency> findByRequirementIdAndDependsOnId(UUID requirementId, UUID dependsOnId);

    void deleteByRequirementIdAndDependsOnId(UUID requirementId, UUID dependsOnId);

    boolean existsByRequirementIdAndDependsOnId(UUID requirementId, UUID dependsOnId);
}
