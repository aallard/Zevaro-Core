package ai.zevaro.core.domain.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByTenantIdAndSpaceIdOrderBySortOrderAsc(UUID tenantId, UUID spaceId);

    List<Document> findByTenantIdAndSpaceIdAndParentDocumentIdIsNullOrderBySortOrderAsc(UUID tenantId, UUID spaceId);

    List<Document> findByTenantIdAndParentDocumentIdOrderBySortOrderAsc(UUID tenantId, UUID parentDocumentId);

    Page<Document> findByTenantIdAndSpaceId(UUID tenantId, UUID spaceId, Pageable pageable);

    Optional<Document> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Document> findByTenantIdAndSpaceIdAndType(UUID tenantId, UUID spaceId, DocumentType type);

    long countByTenantIdAndSpaceId(UUID tenantId, UUID spaceId);

    long countByTenantIdAndParentDocumentId(UUID tenantId, UUID parentDocumentId);

    @Query("SELECT d FROM Document d WHERE d.tenantId = :tenantId AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.body) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Document> search(@Param("tenantId") UUID tenantId, @Param("query") String query, Pageable pageable);
}
