package ai.zevaro.core.domain.decision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionVoteRepository extends JpaRepository<DecisionVote, UUID> {

    List<DecisionVote> findByDecisionId(UUID decisionId);

    Optional<DecisionVote> findByDecisionIdAndUserId(UUID decisionId, UUID userId);

    @Query("SELECT v.vote, COUNT(v) FROM DecisionVote v WHERE v.decision.id = :decisionId GROUP BY v.vote")
    List<Object[]> countVotesByDecision(@Param("decisionId") UUID decisionId);

    long countByDecisionIdAndVote(UUID decisionId, VoteType vote);

    void deleteByDecisionId(UUID decisionId);
}
