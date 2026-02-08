package ai.zevaro.core.domain.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    List<DocumentVersion> findByDocumentIdOrderByVersionDesc(UUID documentId);

    Optional<DocumentVersion> findByDocumentIdAndVersion(UUID documentId, int version);

    Optional<DocumentVersion> findTopByDocumentIdOrderByVersionDesc(UUID documentId);

    void deleteByDocumentId(UUID documentId);
}
