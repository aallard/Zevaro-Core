package ai.zevaro.core.domain.link;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entity_links",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source_type", "source_id", "target_type", "target_id", "link_type"}),
        indexes = {
                @Index(name = "idx_entity_links_source", columnList = "tenant_id, source_type, source_id"),
                @Index(name = "idx_entity_links_target", columnList = "tenant_id, target_type, target_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class EntityLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private EntityType sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private EntityType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false)
    private LinkType linkType;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
