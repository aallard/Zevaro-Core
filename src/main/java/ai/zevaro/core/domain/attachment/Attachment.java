package ai.zevaro.core.domain.attachment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachments_tenant_parent", columnList = "tenant_id, parent_type, parent_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parent_type", nullable = false)
    private AttachmentParentType parentType;

    @Column(name = "parent_id", nullable = false)
    private UUID parentId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "storage_url", nullable = false, length = 1000)
    private String storageUrl;

    @Column(name = "uploaded_by_id", nullable = false)
    private UUID uploadedById;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
