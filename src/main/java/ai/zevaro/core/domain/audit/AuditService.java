package ai.zevaro.core.domain.audit;

import ai.zevaro.core.config.KafkaProducerService;
import ai.zevaro.core.domain.audit.dto.AuditLogFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final String AUDIT_TOPIC = "zevaro.audit.events";

    @Async
    public void log(AuditLogBuilder builder) {
        try {
            AuditLog auditLog = builder.build();
            auditLogRepository.save(auditLog);

            // Publish to Kafka using the circuit breaker protected service
            kafkaProducerService.send(AUDIT_TOPIC, auditLog.getTenantId().toString(), auditLog);

        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    public Page<AuditLog> getAuditLogs(UUID tenantId, AuditLogFilter filter, Pageable pageable) {
        if (filter.actorId() != null) {
            return auditLogRepository.findByTenantIdAndActorIdOrderByTimestampDesc(
                    tenantId, filter.actorId(), pageable);
        }
        if (filter.entityType() != null && filter.entityId() != null) {
            return auditLogRepository.findByTenantIdAndEntityTypeAndEntityIdOrderByTimestampDesc(
                    tenantId, filter.entityType(), filter.entityId(), pageable);
        }
        if (filter.action() != null) {
            return auditLogRepository.findByTenantIdAndActionOrderByTimestampDesc(
                    tenantId, filter.action(), pageable);
        }
        if (filter.startDate() != null && filter.endDate() != null) {
            return auditLogRepository.findByTenantIdAndTimestampBetween(
                    tenantId, filter.startDate(), filter.endDate(), pageable);
        }
        return auditLogRepository.findByTenantIdOrderByTimestampDesc(tenantId, pageable);
    }

    public Map<AuditAction, Long> getActionCounts(UUID tenantId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        List<Object[]> results = auditLogRepository.countByActionSince(tenantId, since);

        Map<AuditAction, Long> counts = new EnumMap<>(AuditAction.class);
        for (AuditAction action : AuditAction.values()) {
            counts.put(action, 0L);
        }

        for (Object[] result : results) {
            AuditAction action = (AuditAction) result[0];
            Long count = (Long) result[1];
            counts.put(action, count);
        }

        return counts;
    }
}
