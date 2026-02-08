package ai.zevaro.core.domain.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_versions", indexes = {
        @Index(name = "idx_doc_versions_document", columnList = "document_id"),
        @Index(name = "idx_doc_versions_doc_ver", columnList = "document_id, version")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Column(name = "edited_by_id", nullable = false)
    private UUID editedById;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
