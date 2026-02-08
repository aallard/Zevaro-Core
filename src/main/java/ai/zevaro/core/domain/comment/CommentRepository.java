package ai.zevaro.core.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTenantIdAndParentTypeAndParentIdOrderByCreatedAtAsc(UUID tenantId, CommentParentType parentType, UUID parentId);

    Page<Comment> findByTenantIdAndParentTypeAndParentId(UUID tenantId, CommentParentType parentType, UUID parentId, Pageable pageable);

    Optional<Comment> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Comment> findByParentCommentId(UUID parentCommentId);

    long countByTenantIdAndParentTypeAndParentId(UUID tenantId, CommentParentType parentType, UUID parentId);
}
