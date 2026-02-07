package ai.zevaro.core.domain.experiment;

import ai.zevaro.core.domain.hypothesis.Hypothesis;
import ai.zevaro.core.domain.project.Project;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "experiments", indexes = {
        @Index(name = "idx_experiment_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_experiment_tenant_project", columnList = "tenant_id, project_id"),
        @Index(name = "idx_experiment_hypothesis", columnList = "hypothesis_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hypothesis_id")
    private Hypothesis hypothesis;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperimentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperimentStatus status = ExperimentStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String config;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(columnDefinition = "TEXT")
    private String results;

    @Column(columnDefinition = "TEXT")
    private String conclusion;

    @Column(name = "traffic_split", length = 20)
    private String trafficSplit;

    @Column(name = "primary_metric", length = 255)
    private String primaryMetric;

    @Column(name = "secondary_metrics", columnDefinition = "TEXT")
    private String secondaryMetrics;

    @Column(name = "audience_filter", columnDefinition = "TEXT")
    private String audienceFilter;

    @Column(name = "sample_size_target")
    private Integer sampleSizeTarget;

    @Column(name = "current_sample_size")
    private Integer currentSampleSize = 0;

    @Column(name = "control_value", precision = 10, scale = 4)
    private BigDecimal controlValue;

    @Column(name = "variant_value", precision = 10, scale = 4)
    private BigDecimal variantValue;

    @Column(name = "confidence_level", precision = 5, scale = 2)
    private BigDecimal confidenceLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "created_by_id")
    private UUID createdById;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
