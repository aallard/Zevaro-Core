package ai.zevaro.core.domain.ticket;

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
@Table(name = "tickets", indexes = {
    @Index(name = "idx_tickets_tenant_ws", columnList = "tenant_id, workstream_id"),
    @Index(name = "idx_tickets_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_tickets_tenant_severity", columnList = "tenant_id, severity"),
    @Index(name = "idx_tickets_tenant_type", columnList = "tenant_id, type"),
    @Index(name = "idx_tickets_tenant_program", columnList = "tenant_id, program_id"),
    @Index(name = "idx_tickets_tenant_assigned", columnList = "tenant_id, assigned_to_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "workstream_id", nullable = false)
    private UUID workstreamId;

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(length = 20, nullable = false)
    private String identifier;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType type;

    @Enumerated(EnumType.STRING)
    private TicketSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.NEW;

    @Enumerated(EnumType.STRING)
    private TicketResolution resolution;

    @Column(name = "reported_by_id", nullable = false)
    private UUID reportedById;

    @Column(name = "assigned_to_id")
    private UUID assignedToId;

    @Column(length = 100)
    private String environment;

    @Column(name = "steps_to_reproduce", columnDefinition = "TEXT")
    private String stepsToReproduce;

    @Column(name = "expected_behavior", columnDefinition = "TEXT")
    private String expectedBehavior;

    @Column(name = "actual_behavior", columnDefinition = "TEXT")
    private String actualBehavior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketSource source = TicketSource.MANUAL;

    @Column(name = "external_ref", length = 255)
    private String externalRef;

    @Column(name = "estimated_hours")
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours")
    private BigDecimal actualHours;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_id")
    private UUID createdById;
}
