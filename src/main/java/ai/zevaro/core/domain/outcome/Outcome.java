package ai.zevaro.core.domain.outcome;

import ai.zevaro.core.domain.team.Team;
import ai.zevaro.core.domain.user.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "outcomes", indexes = {
        @Index(name = "idx_outcome_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_outcome_tenant_team", columnList = "tenant_id, team_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Outcome {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "success_criteria", columnDefinition = "TEXT")
    private String successCriteria;

    @Column(name = "target_metrics", columnDefinition = "text")
    private String targetMetrics;

    @Column(name = "current_metrics", columnDefinition = "text")
    private String currentMetrics;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutcomeStatus status = OutcomeStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutcomePriority priority = OutcomePriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;

    @Column(name = "validation_notes", columnDefinition = "TEXT")
    private String validationNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private User validatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invalidated_by_id")
    private User invalidatedBy;

    @Column(name = "external_refs", columnDefinition = "text")
    private String externalRefs;

    @Column(columnDefinition = "text")
    private String tags;

    @OneToMany(mappedBy = "outcome", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KeyResult> keyResults = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_id")
    private UUID createdById;
}
