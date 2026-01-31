package ai.zevaro.core.domain.stakeholder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StakeholderResponseRepository extends JpaRepository<StakeholderResponse, UUID> {

    List<StakeholderResponse> findByDecisionId(UUID decisionId);

    List<StakeholderResponse> findByStakeholderId(UUID stakeholderId);

    Optional<StakeholderResponse> findByDecisionIdAndStakeholderId(UUID decisionId, UUID stakeholderId);

    List<StakeholderResponse> findByStakeholderIdAndRespondedAtIsNull(UUID stakeholderId);

    @Query("SELECT AVG(sr.responseTimeHours) FROM StakeholderResponse sr WHERE sr.stakeholder.id = :stakeholderId AND sr.respondedAt IS NOT NULL")
    Double findAvgResponseTimeByStakeholder(@Param("stakeholderId") UUID stakeholderId);

    @Query("SELECT COUNT(sr) FROM StakeholderResponse sr WHERE sr.stakeholder.id = :stakeholderId AND sr.withinSla = true")
    Long countWithinSlaByStakeholder(@Param("stakeholderId") UUID stakeholderId);
}
