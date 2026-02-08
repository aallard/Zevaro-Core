package ai.zevaro.core.domain.activity;

import ai.zevaro.core.domain.activity.dto.ActivityEvent;
import ai.zevaro.core.domain.audit.AuditLog;
import ai.zevaro.core.domain.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Page<ActivityEvent> getActivity(UUID tenantId, UUID programId, UUID workstreamId,
                                            String entityType, Pageable pageable) {
        Page<AuditLog> logs;

        if (entityType != null && !entityType.isBlank()) {
            logs = auditLogRepository.findByTenantIdAndEntityTypeOrderByTimestampDesc(
                    tenantId, entityType, pageable);
        } else {
            logs = auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId, pageable);
        }

        return logs.map(this::toActivityEvent);
    }

    private ActivityEvent toActivityEvent(AuditLog log) {
        return new ActivityEvent(
                log.getId(),
                log.getActorId(),
                log.getActorName(),
                log.getAction() != null ? log.getAction().name() : null,
                log.getEntityType(),
                log.getEntityId(),
                log.getEntityTitle(),
                log.getTimestamp(),
                log.getDescription()
        );
    }
}
