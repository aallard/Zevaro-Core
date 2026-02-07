package ai.zevaro.core.domain.stakeholder;

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
import jakarta.persistence.OneToOne;
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
@Table(name = "stakeholders", indexes = {
        @Index(name = "idx_stakeholder_tenant", columnList = "tenant_id"),
        @Index(name = "idx_stakeholder_user", columnList = "user_id"),
        @Index(name = "idx_stakeholder_project", columnList = "project_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Stakeholder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String title;

    private String organization;

    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StakeholderType type = StakeholderType.INTERNAL;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "text")
    private String expertise;

    @Column(name = "preferred_contact_method")
    private String preferredContactMethod;

    @Column(name = "availability_notes", columnDefinition = "TEXT")
    private String availabilityNotes;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "decisions_pending")
    private Integer decisionsPending = 0;

    @Column(name = "decisions_completed")
    private Integer decisionsCompleted = 0;

    @Column(name = "decisions_escalated")
    private Integer decisionsEscalated = 0;

    @Column(name = "avg_response_time_hours")
    private Double avgResponseTimeHours;

    @Column(name = "last_decision_at")
    private Instant lastDecisionAt;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "external_refs", columnDefinition = "text")
    private String externalRefs;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by_id")
    private UUID createdById;
}
