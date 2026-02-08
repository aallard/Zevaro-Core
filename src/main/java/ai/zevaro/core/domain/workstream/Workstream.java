package ai.zevaro.core.domain.workstream;

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
@Table(name = "workstreams",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "program_id", "name"}),
    indexes = {
        @Index(name = "idx_workstreams_tenant_program", columnList = "tenant_id, program_id"),
        @Index(name = "idx_workstreams_tenant_status", columnList = "tenant_id, status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Workstream {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkstreamMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_mode", nullable = false)
    private ExecutionMode executionMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkstreamStatus status = WorkstreamStatus.NOT_STARTED;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(columnDefinition = "text")
    private String tags;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_id")
    private UUID createdById;
}
