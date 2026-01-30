package ai.zevaro.core.domain.decision;

import ai.zevaro.core.domain.hypothesis.Hypothesis;
import ai.zevaro.core.domain.outcome.Outcome;
import ai.zevaro.core.domain.team.Team;
import ai.zevaro.core.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "decisions", indexes = {
        @Index(name = "idx_decision_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_decision_tenant_priority", columnList = "tenant_id, priority"),
        @Index(name = "idx_decision_owner", columnList = "owner_id"),
        @Index(name = "idx_decision_assigned_to", columnList = "assigned_to_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Column(columnDefinition = "text")
    private String options;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionStatus status = DecisionStatus.NEEDS_INPUT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionPriority priority = DecisionPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type")
    private DecisionType decisionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outcome_id")
    private Outcome outcome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hypothesis_id")
    private Hypothesis hypothesis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "escalation_level")
    private Integer escalationLevel = 0;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to_id")
    private User escalatedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_id")
    private User decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_rationale", columnDefinition = "TEXT")
    private String decisionRationale;

    @Column(name = "selected_option", columnDefinition = "text")
    private String selectedOption;

    @Column(name = "blocked_items", columnDefinition = "text")
    private String blockedItems;

    @Column(name = "external_refs", columnDefinition = "text")
    private String externalRefs;

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

    public boolean isOverdue() {
        return dueAt != null && Instant.now().isAfter(dueAt)
                && status != DecisionStatus.DECIDED
                && status != DecisionStatus.IMPLEMENTED
                && status != DecisionStatus.CANCELLED;
    }

    public long getWaitTimeHours() {
        if (createdAt == null) {
            return 0;
        }
        return Duration.between(createdAt, Instant.now()).toHours();
    }
}
