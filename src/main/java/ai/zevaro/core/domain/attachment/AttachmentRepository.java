package ai.zevaro.core.domain.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByTenantIdAndParentTypeAndParentIdOrderByCreatedAtDesc(UUID tenantId, AttachmentParentType parentType, UUID parentId);

    Optional<Attachment> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantIdAndParentTypeAndParentId(UUID tenantId, AttachmentParentType parentType, UUID parentId);

    void deleteByTenantIdAndParentTypeAndParentId(UUID tenantId, AttachmentParentType parentType, UUID parentId);
}
