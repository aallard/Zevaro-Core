package ai.zevaro.core.domain.requirement;

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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requirements", indexes = {
    @Index(name = "idx_requirements_tenant_spec", columnList = "tenant_id, specification_id"),
    @Index(name = "idx_requirements_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_requirements_tenant_ws", columnList = "tenant_id, workstream_id"),
    @Index(name = "idx_requirements_tenant_program", columnList = "tenant_id, program_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Requirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "specification_id", nullable = false)
    private UUID specificationId;

    @Column(name = "workstream_id", nullable = false)
    private UUID workstreamId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(nullable = false, length = 20)
    private String identifier;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequirementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequirementPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequirementStatus status = RequirementStatus.DRAFT;

    @Column(name = "acceptance_criteria", columnDefinition = "TEXT")
    private String acceptanceCriteria;

    @Column(name = "estimated_hours")
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours")
    private BigDecimal actualHours;

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
