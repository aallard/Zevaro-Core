package ai.zevaro.core.domain.hypothesis;

import ai.zevaro.core.domain.outcome.Outcome;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hypotheses", indexes = {
        @Index(name = "idx_hypothesis_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_hypothesis_tenant_outcome", columnList = "tenant_id, outcome_id"),
        @Index(name = "idx_hypothesis_tenant_owner", columnList = "tenant_id, owner_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Hypothesis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outcome_id", nullable = false)
    private Outcome outcome;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String belief;

    @Column(name = "expected_result", nullable = false, columnDefinition = "TEXT")
    private String expectedResult;

    @Column(name = "measurement_criteria", columnDefinition = "TEXT")
    private String measurementCriteria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HypothesisStatus status = HypothesisStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HypothesisPriority priority = HypothesisPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TShirtSize effort;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TShirtSize impact;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ConfidenceLevel confidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "experiment_config", columnDefinition = "text")
    private String experimentConfig;

    @Column(name = "experiment_results", columnDefinition = "text")
    private String experimentResults;

    @Column(name = "blocked_reason", columnDefinition = "TEXT")
    private String blockedReason;

    @Column(name = "conclusion_notes", columnDefinition = "TEXT")
    private String conclusionNotes;

    @Column(name = "external_refs", columnDefinition = "text")
    private String externalRefs;

    @Column(columnDefinition = "text")
    private String tags;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "deployed_at")
    private Instant deployedAt;

    @Column(name = "measuring_started_at")
    private Instant measuringStartedAt;

    @Column(name = "concluded_at")
    private Instant concludedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concluded_by_id")
    private User concludedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_id")
    private UUID createdById;
}
