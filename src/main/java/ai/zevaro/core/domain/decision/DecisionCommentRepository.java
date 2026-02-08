package ai.zevaro.core.domain.decision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// Replaced by polymorphic Comment entity in domain/comment/. Will be removed after data migration in ZC-064.
@Deprecated
@Repository
public interface DecisionCommentRepository extends JpaRepository<DecisionComment, UUID> {

    List<DecisionComment> findByDecisionIdOrderByCreatedAtAsc(UUID decisionId);

    List<DecisionComment> findByDecisionIdAndParentIsNullOrderByCreatedAtAsc(UUID decisionId);

    List<DecisionComment> findByParentId(UUID parentId);

    int countByDecisionId(UUID decisionId);
}
