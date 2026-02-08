package ai.zevaro.core.domain.stakeholder;

import ai.zevaro.core.domain.decision.Decision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stakeholder_responses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"decision_id", "stakeholder_id"}),
    indexes = {
        @Index(name = "idx_response_decision", columnList = "decision_id"),
        @Index(name = "idx_response_stakeholder", columnList = "stakeholder_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StakeholderResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stakeholder_id", nullable = false)
    private Stakeholder stakeholder;

    @Column(length = 2000)
    private String response;

    @Column(name = "response_time_hours", precision = 10, scale = 2)
    private BigDecimal responseTimeHours;

    @Column(name = "within_sla")
    private Boolean withinSla;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
