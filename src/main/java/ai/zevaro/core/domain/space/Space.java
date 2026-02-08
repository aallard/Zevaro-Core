package ai.zevaro.core.domain.space;

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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "spaces",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "name"})
        },
        indexes = {
                @Index(name = "idx_spaces_tenant", columnList = "tenant_id"),
                @Index(name = "idx_spaces_tenant_type", columnList = "tenant_id, type"),
                @Index(name = "idx_spaces_tenant_program", columnList = "tenant_id, program_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 300)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceStatus status = SpaceStatus.ACTIVE;

    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(length = 10)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceVisibility visibility = SpaceVisibility.PUBLIC;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_id")
    private UUID createdById;
}
