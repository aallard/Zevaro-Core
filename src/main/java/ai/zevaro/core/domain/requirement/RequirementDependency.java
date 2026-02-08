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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requirement_dependencies",
    uniqueConstraints = @UniqueConstraint(columnNames = {"requirement_id", "depends_on_id"}),
    indexes = {
        @Index(name = "idx_req_dep_requirement", columnList = "requirement_id"),
        @Index(name = "idx_req_dep_depends_on", columnList = "depends_on_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class RequirementDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "requirement_id", nullable = false)
    private UUID requirementId;

    @Column(name = "depends_on_id", nullable = false)
    private UUID dependsOnId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DependencyType type;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
